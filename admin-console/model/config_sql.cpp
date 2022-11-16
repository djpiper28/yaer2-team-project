#include <iostream>
#include "configuration.h"
#include "../security.h"
#include "../utils.h"

std::string Configuration::getDbConnString()
{
    std::string ret = "dbname = " + this->db_name +
                      " user = " + this->db_username +
                      " password = " + this->db_password +
                      " hostaddr = " + resolveHostName(this->db_host_address.c_str()) +
                      " port = " + std::to_string(this->db_port);

    return ret;
}

pqxx::connection *Configuration::getConnection()
{
    bool open = this->conn != nullptr;
    if (open) {
        open = this->conn->is_open();
    }

    if (open) {
        return this->conn;
    } else {
        std::cerr << "Connecting to: "
                  << this->getDbConnString()
                  << std::endl;
        delete this->conn;
        this->conn = new pqxx::connection(this->getDbConnString());

        std::cerr << "Preparing statments.. ";

        // Menu items
        this->conn->prepare(INV_IN_STOCK_STATEMENT,
                            "select inventoryitems.amount, inventorymenu.unitsrequired "
                            "from inventorymenu, inventoryitems "
                            "where inventorymenu.menuid = $1 "
                            "and inventorymenu.invid = inventoryitems.invid;");
        this->conn->prepare(MENU_ITEM_ADD_STATEMENT,
                            "insert into menuitems (menuid, name, description, price, imageuri, "
                            "active, typeid, prep_time) "
                            "values ($1, $2, $3, $4, $5, $6, $7, $8);");
        this->conn->prepare(MENU_ITEM_UPDATE_STATEMENT,
                            "update menuitems set name = $2, description = $3, price = $4, "
                            "imageuri = $5, active = $6, typeid = $7, prep_time = $8 where menuid = $1;");

        // Inv items
        this->conn->prepare(INV_GET_USED_BY_STATEMENT,
                            "select menuitems.name, menuitems.description, "
                            "menuitems.imageuri, menuitems.menuid, menuitems.price, menuitems.active, "
                            "menuitems.typeid, menuitems.prep_time, "
                            "inventoryitems.invid, inventoryitems.itemname, "
                            "inventoryitems.amount, inventorymenu.unitsrequired "
                            "from menuitems, inventoryitems, inventorymenu "
                            "where inventoryitems.invid = inventorymenu.invid "
                            "and menuitems.menuid = inventorymenu.menuid;");
        this->conn->prepare(INV_ITEM_UPDATE_STATEMENT,
                            "update inventoryitems set itemname = $2, amount = $3 "
                            "where invid = $1;");
        this->conn->prepare(INV_ADD_ITEM_STATEMENT,
                            "insert into inventoryitems (invid, itemname, amount) "
                            "values ($1, $2, $3);");

        // Inv menu items
        this->conn->prepare(INV_MENU_GET_STATEMENT,
                            "select inventorymenu.invid, itemname, amount, unitsrequired "
                            "from inventoryitems, inventorymenu "
                            "where inventoryitems.invid = inventorymenu.invid "
                            "and inventorymenu.menuid = $1;");
        this->conn->prepare(INV_MENU_ADD_STATEMTNT,
                            "insert into inventorymenu(invid, menuid, unitsrequired) "
                            "values ($1, $2, $3);");
        this->conn->prepare(INV_MENU_UPDATE_STATEMNT,
                            "update inventorymenu set unitsrequired = $3 "
                            "where invid = $1 and menuid = $2;");
        this->conn->prepare(INV_MENU_DELETE_STATEMNT,
                            "delete from inventorymenu "
                            "where invid = $1 and menuid = $2;");

        // Menu types
        this->conn->prepare(MENU_TYPE_ADD_STATEMNT,
                            "insert into menutypes (typeid, item_type, item_image, item_desc) "
                            "values ($1, $2, $3, $4);");
        this->conn->prepare(MENU_TYPE_UPDATE_STATEMNT,
                            "update menutypes set item_type = $2, item_image = $3, item_desc = $4 "
                            "where typeid = $1;");
        this->conn->prepare(MENU_TYPE_DELETE_STATEMENT,
                            "delete from menutypes where typeid = $1;");

        // User
        this->conn->prepare(USER_UPDATE_STATEMENT,
                            "update users set firstname = $2, lastname = $3, phoneno = $4, usetype = $5 "
                            "where userid = $1;");
        this->conn->prepare(USER_CHANGE_PASSWORD,
                            "update users set password = $2, salt = $3 where userid = $1;");
        this->conn->prepare(USER_ADD_STATEMENT,
                            "insert into users(userid, firstname, lastname, email, phoneno, "
                            "usetype, password, salt) values ($1, $2, $3, $4, $5, $6, $7, $8);");
        this->conn->prepare(USER_GET_PASSWORD,
                            "select password, salt from users where userid = $1;");

        std::cerr << "Done" << std::endl;
        return this->conn;
    }
}

bool Configuration::deleteMenuType(MenuType type)
{
    try {
        pqxx::connection *conn = this->getConnection();
        pqxx::work t(*conn);
        t.exec_prepared(MENU_TYPE_DELETE_STATEMENT,
                        type.getUUID());
        t.commit();

        std::cout << "Deleted "
                  << type.getUUID()
                  << "("
                  << type.getName()
                  << ")"
                  << std::endl;
    } catch (const std::exception &e) {
        std::cerr << e.what() << " @ " << __FILE__ << ":" << __LINE__ << std::endl;
        return false;
    }

    return true;
}

bool Configuration::addMenuType(MenuType type)
{
    try {
        pqxx::connection *conn = this->getConnection();
        pqxx::work t(*conn);
        t.exec_prepared(MENU_TYPE_ADD_STATEMNT,
                        type.getUUID(),
                        type.getName(),
                        type.getImage(),
                        type.getDesc());
        t.commit();

        std::cout << "Added "
                  << type.getUUID()
                  << "("
                  << type.getName()
                  << ")"
                  << std::endl;
    } catch (const std::exception &e) {
        std::cerr << e.what() << " @ " << __FILE__ << ":" << __LINE__ << std::endl;
        return false;
    }

    return true;
}

bool Configuration::updateMenuType(MenuType oldType, MenuType newType)
{
    try {
        pqxx::connection *conn = this->getConnection();
        pqxx::work t(*conn);
        t.exec_prepared(MENU_TYPE_UPDATE_STATEMNT,
                        oldType.getUUID(),
                        newType.getName(),
                        newType.getImage(),
                        newType.getDesc());
        t.commit();

        std::cout << "Updated "
                  << oldType.getUUID()
                  << "("
                  << oldType.getName()
                  << ")"
                  << std::endl;
    } catch (const std::exception &e) {
        std::cerr << e.what() << std::endl;
        return false;
    }

    return true;
}

std::list<MenuType> Configuration::getMenuTypes()
{
    std::list<MenuType> ret;

    try {
        pqxx::connection *conn = this->getConnection();
        if (conn->is_open()) {
            pqxx::work t(*conn);
            pqxx::result res(t.exec("select typeid, item_type, item_image, item_desc from menutypes;"));

            std::list<MenuItem> tmp;
            for (auto row : res) {
                std::string uuid = row[0].as<std::string>();
                std::string name = row[1].as<std::string>();
                std::string image = row[2].as<std::string>();
                std::string desc = row[3].as<std::string>();

                MenuType type = MenuType(uuid, name, image, desc);
                ret.push_back(type);
            }

            res = t.exec("select menuitems.name, menuitems.description, "
                         "menuitems.imageuri, menuitems.menuid, menuitems.price, menuitems.active, "
                         "menuitems.typeid, menuitems.prep_time "
                         "from menuitems;");

            for (auto row : res) {
                std::string name = row[0].as<std::string>();
                std::string desc = row[1].as<std::string>();
                std::string image_uri = row[2].as<std::string>();
                std::string uuid = row[3].as<std::string>();
                std::string priceTmp = row[4].as<std::string>().substr(2);
                double price = atof(priceTmp.c_str());
                bool active = row[5].as<bool>();
                std::string type_id = row[6].as<std::string>();
                int prepTime = row[7].as<int>();

                MenuItem item(name, desc, image_uri, uuid, type_id, price, active, prepTime);
                tmp.push_back(item);
            }

            t.commit();

            this->menuTypeIdToItemMap = std::map<std::string, std::list<MenuItem>>();

            for (MenuItem item : tmp) {
                MenuItem newitem(item.getName(),
                                 item.getDescription(),
                                 item.getImageURI(),
                                 item.getUUID(),
                                 item.getTypeUUID(),
                                 item.getPrice(),
                                 item.isActive(),
                                 this,
                                 item.getPrepTime());
                this->menuTypeIdToItemMap[item.getTypeUUID()].push_back(newitem);
            }
        }
    } catch (const std::exception &e) {
        std::cerr << e.what() << " @ " << __FILE__ << ":" << __LINE__ << std::endl;
    }

    return ret;
}

std::pair<std::string, std::string> Configuration::getUserPassword(User user)
{
    try {
        pqxx::connection *conn = this->getConnection();
        if (conn->is_open()) {
            pqxx::work t(*conn);
            pqxx::result res = t.exec_prepared(USER_GET_PASSWORD,
                                               user.getUUID());

            std::string password;
            std::string salt;
            for (auto row : res) {
                password = row[0].as<std::string>();
                salt = row[1].as<std::string>();
            }

            t.commit();
            return std::pair<std::string, std::string>(password, salt);
        }
    } catch (const std::exception &e) {
        std::cerr << e.what() << " @ " << __FILE__ << ":" << __LINE__ << std::endl;
        return std::pair<std::string, std::string>("", "");
    }

    return std::pair<std::string, std::string>("", "");
}

std::list<MenuItem> Configuration::getItemsThatAre(MenuType type)
{
    return this->menuTypeIdToItemMap[type.getUUID()];
}

std::list<MenuItem> Configuration::getMenu()
{
    std::list<MenuItem> tmp, ret;

    try {
        pqxx::connection *conn = this->getConnection();
        if (conn->is_open()) {
            pqxx::work t(*conn);
            pqxx::result res(t.exec("select name, description, imageuri, menuid, price, "
                                    "active, typeid, prep_time from menuitems;"));

            for (auto row : res) {
                std::string name = row[0].as<std::string>();
                std::string desc = row[1].as<std::string>();
                std::string image_uri = row[2].as<std::string>();
                std::string uuid = row[3].as<std::string>();
                std::string priceTmp = row[4].as<std::string>().substr(2);
                double price = atof(priceTmp.c_str());
                bool active = row[5].as<bool>();
                std::string type_id = row[6].as<std::string>();
                int prepTime = row[7].as<int>();

                MenuItem item(name,
                              desc,
                              image_uri,
                              uuid,
                              type_id,
                              price,
                              active,
                              prepTime);
                tmp.push_back(item);
            }

            t.commit();

            for (MenuItem item : tmp) {
                MenuItem newitem(item.getName(),
                                 item.getDescription(),
                                 item.getImageURI(),
                                 item.getUUID(),
                                 item.getTypeUUID(),
                                 item.getPrice(),
                                 item.isActive(),
                                 this,
                                 item.getPrepTime());
                ret.push_back(newitem);
            }
        }
    } catch (const std::exception &e) {
        std::cerr << e.what() << " @ " << __FILE__ << ":" << __LINE__ <<std::endl;
    }

    return ret;
}

std::list<User> Configuration::getUsers()
{
    std::list<User> ret;

    try {
        pqxx::connection *conn = this->getConnection();
        if (conn->is_open()) {
            pqxx::work t(*conn);
            pqxx::result res(t.exec("select userid, firstname, lastname, email, phoneno, usetype from users;"));

            for (auto row : res) {
                std::string uuid = row[0].as<std::string>();
                std::string fname = row[1].as<std::string>();
                std::string sname = row[2].as<std::string>();
                std::string email = row[3].as<std::string>();
                std::string phoneno = row[4].as<std::string>();
                int usetype = row[5].as<int>();

                User user = User(uuid, fname, sname, email, phoneno, usetype);
                ret.push_back(user);
            }

            t.commit();
        }
    } catch (const std::exception &e) {
        std::cerr << e.what() << " @ " << __FILE__ << ":" << __LINE__ <<std::endl;
    }

    return ret;
}

std::list<InventoryItem> Configuration::getInvItems()
{
    std::list<InventoryItem> ret;

    try {
        pqxx::connection *conn = this->getConnection();
        if (conn->is_open()) {
            pqxx::work t(*conn);
            pqxx::result res(t.exec("select invid, itemname, amount from inventoryitems;"));

            for (auto row : res) {
                std::string uuid = row[0].as<std::string>();
                std::string name = row[1].as<std::string>();
                int amount = row[2].as<int>();

                InventoryItem item(uuid, name, amount);
                ret.push_back(item);
            }

            t.commit();

            res = t.exec_prepared(INV_GET_USED_BY_STATEMENT);
            this->menuIdToInvMenuItemMap = std::map<std::string, std::list<InventoryMenuItem>>();

            std::list<MenuItem> tmp;
            std::vector<std::string> invids;
            for (auto row : res) {
                std::string name = row[0].as<std::string>();
                std::string desc = row[1].as<std::string>();
                std::string image_uri = row[2].as<std::string>();
                std::string uuid = row[3].as<std::string>();
                std::string priceTmp = row[4].as<std::string>().substr(2);
                double price = atof(priceTmp.c_str());
                bool active = row[5].as<bool>();
                std::string type_id = row[6].as<std::string>();
                int prepTime = row[7].as<int>();
                std::string invid = row[8].as<std::string>();

                std::string invname = row[9].as<std::string>();
                int amount = row[10].as<int>();
                int requiredUnits = row[11].as<int>();

                InventoryMenuItem invmenuitem = InventoryMenuItem(uuid, invid, invname, amount, requiredUnits);
                menuIdToInvMenuItemMap[uuid].push_back(invmenuitem);

                MenuItem item(name, desc, image_uri, uuid, type_id, price, active, prepTime);
                tmp.push_back(item);
                invids.push_back(invid);
            }

            t.commit();

            this->invIdToItemMap = std::map<std::string, std::list<MenuItem>>();

            int i = 0;
            for (MenuItem item : tmp) {
                MenuItem newitem(item.getName(),
                                 item.getDescription(),
                                 item.getImageURI(),
                                 item.getUUID(),
                                 item.getTypeUUID(),
                                 item.getPrice(),
                                 item.isActive(),
                                 this,
                                 item.getPrepTime());
                this->invIdToItemMap[invids[i]].push_back(newitem);
                i++;
            }

            t.commit();
        }
    } catch (const std::exception &e) {
        std::cerr << e.what() << " @ " << __FILE__ << ":" << __LINE__ << std::endl;
    }

    return ret;
}

std::list<MenuItem> Configuration::getItemsThatUse(InventoryItem item)
{
    return this->invIdToItemMap[item.getUUID()];
}

bool Configuration::isInStock(MenuItem item)
{
    bool inStock = true;
    try {
        pqxx::connection *conn = this->getConnection();
        if (conn->is_open()) {
            pqxx::work t(*conn);
            pqxx::result res = t.exec_prepared(INV_IN_STOCK_STATEMENT,
                                               item.getUUID());

            for (auto row : res) {
                int amount = row[0].as<int>();
                int requiredAmount = row[1].as<int>();

                inStock = amount >= requiredAmount;
                if (!inStock) break;
            }

            t.commit();
        }
    } catch (const std::exception &e) {
        std::cerr << e.what() << " @ " << __FILE__ << ":" << __LINE__ <<std::endl;
        return false;
    }

    return inStock;
}

bool Configuration::updateMenuItem(MenuItem oldItem, MenuItem newItem)
{
    try {
        pqxx::connection *conn = this->getConnection();
        pqxx::work t(*conn);
        t.exec_prepared(MENU_ITEM_UPDATE_STATEMENT,
                        oldItem.getUUID(),
                        newItem.getName(),
                        newItem.getDescription(),
                        newItem.getPrice(),
                        newItem.getImageURI(),
                        newItem.isActive(),
                        newItem.getTypeUUID(),
                        newItem.getPrepTime());
        t.commit();

        std::cout << "Updated menu item "
                  << oldItem.getUUID()
                  << "("
                  << newItem.getName()
                  << ")"
                  << std::endl;
    } catch (const std::exception &e) {
        std::cerr << e.what() << " @ " << __FILE__ << ":" << __LINE__ <<std::endl;
        return false;
    }

    return true;
}

bool Configuration::updateInventoryItem(InventoryItem oldItem, InventoryItem newItem)
{
    try {
        pqxx::connection *conn = this->getConnection();
        pqxx::work t(*conn);
        t.exec_prepared(INV_ITEM_UPDATE_STATEMENT,
                        oldItem.getUUID(),
                        newItem.getName(),
                        newItem.getAmount());
        t.commit();

        std::cout << "Updated inventory item "
                  << oldItem.getUUID()
                  << "("
                  << newItem.getName()
                  << ")"
                  << std::endl;
    } catch (const std::exception &e) {
        std::cerr << e.what() << std::endl;
        return false;
    }

    return true;
}

bool Configuration::updateUser(User oldUser, User newUser)
{
    try {
        pqxx::connection *conn = this->getConnection();
        pqxx::work t(*conn);
        t.exec_prepared(USER_UPDATE_STATEMENT,
                        oldUser.getUUID(),
                        newUser.getFName(),
                        newUser.getSName(),
                        newUser.getPhoneNo(),
                        newUser.getUseType());
        t.commit();

        std::cout << "Updated user "
                  << newUser.getUUID()
                  << "('"
                  << newUser.getFName()
                  << "' '"
                  << newUser.getSName()
                  << "')"
                  << std::endl;
    } catch(const std::exception &e) {
        std::cerr << e.what() << " @ " << __FILE__ << ":" << __LINE__ <<std::endl;
        return false;
    }

    return true;
}

bool Configuration::updateUserPassword(User user, std::string password)
{
    std::string salt = getSalt();
    int s = 0;
    std::string hashedPassword = hashPassword(password, salt, &s);
    if (!s) {
        std::cerr << "Failed to hash passsword" << std::endl;
        return false;
    }

    try {
        pqxx::connection *conn = this->getConnection();
        pqxx::work t(*conn);
        t.exec_prepared(USER_CHANGE_PASSWORD,
                        user.getUUID(),
                        hashedPassword,
                        salt);
        t.commit();

        std::cout << "Updated user password "
                  << user.getUUID()
                  << "('"
                  << user.getFName()
                  << "' '"
                  << user.getSName()
                  << "')"
                  << std::endl;
    } catch(const std::exception &e) {
        std::cerr << e.what() << " @ " << __FILE__ << ":" << __LINE__ <<std::endl;
        return false;
    }

    return true;
}

bool Configuration::addMenuItem(MenuItem item)
{
    try {
        pqxx::connection *conn = this->getConnection();
        pqxx::work t(*conn);
        t.exec_prepared(MENU_ITEM_ADD_STATEMENT,
                        item.getUUID(),
                        item.getName(),
                        item.getDescription(),
                        item.getPrice(),
                        item.getImageURI(),
                        item.isActive(),
                        item.getTypeUUID(),
                        item.getPrepTime());
        t.commit();

        std::cout << "Added "
                  << item.getUUID()
                  << "("
                  << item.getName()
                  << ")"
                  << std::endl;
    } catch (const std::exception &e) {
        std::cerr << e.what() << " @ " << __FILE__ << ":" << __LINE__ <<std::endl;
        return false;
    }

    return true;
}

bool Configuration::addInvItem(InventoryItem item)
{
    try {
        pqxx::connection *conn = this->getConnection();
        pqxx::work t(*conn);
        t.exec_prepared(INV_ADD_ITEM_STATEMENT,
                        item.getUUID(),
                        item.getName(),
                        item.getAmount());
        t.commit();

        std::cout << "Added "
                  << item.getUUID()
                  << "("
                  << item.getName()
                  << ")"
                  << std::endl;
    } catch (const std::exception &e) {
        std::cerr << e.what() << " @ " << __FILE__ << ":" << __LINE__ <<std::endl;
        return false;
    }

    return true;
}

bool Configuration::addUser(User user, std::string password)
{
    std::string salt = getSalt();
    int status = 0;
    std::string hashedPassword = hashPassword(password, salt, &status);

    if (!status) {
        std::cerr << "Failed to hash password" << std::endl;
        return false;
    }

    try {
        pqxx::connection *conn = this->getConnection();
        pqxx::work t(*conn);
        t.exec_prepared(USER_ADD_STATEMENT,
                        user.getUUID(),
                        user.getFName(),
                        user.getSName(),
                        user.getEmail(),
                        user.getPhoneNo(),
                        user.getUseType(),
                        hashedPassword,
                        salt);
        t.commit();

        std::cout << "Added user "
                  << user.getUUID()
                  << "("
                  << user.getFName()
                  << " "
                  << user.getSName()
                  << ")"
                  << std::endl;
    } catch (const std::exception &e) {
        std::cerr << e.what() << " @ " << __FILE__ << ":" << __LINE__ <<std::endl;
        return false;
    }

    return true;
}

std::list<InventoryMenuItem> Configuration::getInvMenuItemsFor(MenuItem item)
{
    return this->menuIdToInvMenuItemMap[item.getUUID()];
}

bool Configuration::updateInvMenuItems(MenuItem menuitem, std::list<InventoryMenuItem> invItems)
{
    try {
        pqxx::connection *conn = this->getConnection();
        if (conn->is_open()) {
            pqxx::work t(*conn);
            pqxx::result res = t.exec_prepared(INV_MENU_GET_STATEMENT,
                                               menuitem.getUUID());

            // Get current items in the database
            std::list<InventoryMenuItem> needUpdating, needDeleting, needAdding, all;
            for (auto row : res) {
                std::string invid= row[0].as<std::string>();
                std::string name = row[1].as<std::string>();
                int amount = row[2].as<int>();
                int amountRequired = row[3].as<int>();

                bool found = false; // is old in new?
                InventoryMenuItem invmenuitem;
                for (InventoryMenuItem item: invItems) {
                    if (item.getInvUUID() == invid) {
                        found = true;
                        invmenuitem = item;
                        break;
                    }
                }

                if (!found) {
                    invmenuitem = InventoryMenuItem(menuitem.getUUID(),
                                                    invid,
                                                    name,
                                                    amount,
                                                    amountRequired);
                }

                all.push_back(invmenuitem);
                if (found) {
                    needUpdating.push_back(invmenuitem);
                } else if (!found) {
                    needDeleting.push_back(invmenuitem);
                }
            }

            // All items that are not in the complete list of items need to be added
            while (invItems.size() > 0) {
                InventoryMenuItem newItem = invItems.back();
                invItems.pop_back();

                bool found = false;
                for (InventoryMenuItem item : all) {
                    if (item.getInvUUID() == newItem.getInvUUID()) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    needAdding.push_back(newItem);
                }
            }

            // Remove all that are no longer there
            for (InventoryMenuItem item : needDeleting) {
                std::cout << "Deleting "
                          << item.getName()
                          << std::endl;
                pqxx::result res = t.exec_prepared(INV_MENU_DELETE_STATEMNT,
                                                   item.getInvUUID(),
                                                   menuitem.getUUID());

                if (res.affected_rows() != 1) {
                    std::cerr << "Failed to delete "
                              << item.getName()
                              << std::endl;
                    t.abort();
                }
            }

            // Update all that are needed
            for (InventoryMenuItem item : needUpdating) {
                std::cout << "Updating "
                          << item.getName()
                          << " | "
                          << item.getUnitsRequired()
                          << std::endl;
                pqxx::result res = t.exec_prepared(INV_MENU_UPDATE_STATEMNT,
                                                   item.getInvUUID(),
                                                   menuitem.getUUID(),
                                                   item.getUnitsRequired());

                if (res.affected_rows() != 1) {
                    std::cerr << "Failed to update "
                              << item.getName()
                              << std::endl;
                    t.abort();
                }
            }

            // Add all that are needed
            for (InventoryMenuItem item : needAdding) {
                std::cout << "Adding "
                          << item.getName()
                          << std::endl;
                pqxx::result res = t.exec_prepared(INV_MENU_ADD_STATEMTNT,
                                                   item.getInvUUID(),
                                                   menuitem.getUUID(),
                                                   item.getUnitsRequired());
            }

            // Commit at the very end
            t.commit();
        }
    } catch (const std::exception &e) {
        std::cerr << e.what() << " @ " << __FILE__ << ":" << __LINE__ <<std::endl;
        return false;
    }

    return true;
}
