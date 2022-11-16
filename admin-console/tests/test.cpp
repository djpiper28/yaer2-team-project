#include <iostream>
#include <unistd.h>
#include <stdio.h>
#include <list>
#include "../model/configuration.h"
#include "test.h"
#include "testing.h"
#include "../ui/filteredlist.h"
#include "security.h"

bool testConfigSave()
{
    Configuration config;
    config.setDbName(TEST_ENV);

    return config.save();
}

bool testConifgLoad()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    try {
        pqxx::connection *conn = config.getConnection();
        if (conn->is_open()) {
            pqxx::work t(*conn);
            pqxx::result res(t.exec("select * from menuitems;"));
            t.commit();
        }
        conn->close();
    } catch (const std::exception &e) {
        std::cerr << "Failed to connect to the database and execute a command." << std::endl
                  << e.what() << std::endl;
        return false;
    }

    return true;
}

bool testGetMenu()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    std::list<MenuItem> menu = config.getMenu();
    ASSERT(menu.size() > 0);

    return true;
}

bool testUpdateMenu()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    std::list<MenuItem> menu = config.getMenu();
    ASSERT(menu.size() > 0);

    std::string tid = config.getMenuTypes().front().getUUID();
    MenuItem item = menu.front();
    MenuItem item2 = MenuItem(TEST_STR, TEST_STR, TEST_STR, tid, TEST_NUM, TEST_BOOL, &config, TEST_NUM);
    ASSERT(config.updateMenuItem(item, item2));

    std::list<MenuItem> updatedMenu = config.getMenu();
    bool found = false;
    for (MenuItem newItem : updatedMenu) {
        if (newItem.getUUID() == item.getUUID()) {
            ASSERT(newItem.getName() == TEST_STR);
            ASSERT(newItem.getDescription() == TEST_STR);
            ASSERT(newItem.getImageURI() == TEST_STR);
            ASSERT(newItem.getPrice() == TEST_NUM);
            ASSERT(newItem.isActive() == TEST_BOOL);
            ASSERT(newItem.getPrepTime() == TEST_NUM);
            found = true;
            break;
        }
    }

    return found;
}

bool testAddToMenu()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    std::string tid = config.getMenuTypes().front().getUUID();

    MenuItem item = MenuItem(TEST_STR, TEST_STR, TEST_STR, tid, TEST_NUM, TEST_BOOL, &config, TEST_NUM);
    ASSERT(config.addMenuItem(item));

    std::list<MenuItem> menu = config.getMenu();
    ASSERT(menu.size() > 0);

    bool found = false;
    for (MenuItem newItem : menu) {
        if (newItem.getUUID() == item.getUUID()) {
            ASSERT(newItem.getName() == TEST_STR);
            ASSERT(newItem.getDescription() == TEST_STR);
            ASSERT(newItem.getImageURI() == TEST_STR);
            ASSERT(newItem.getPrice() == TEST_NUM);
            ASSERT(newItem.isActive() == TEST_BOOL);
            ASSERT(newItem.getPrepTime() == TEST_NUM);
            found = true;
            break;
        }
    }

    return found;
}

bool testToCdnPathNoCopy()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    std::string path = config.toCdnPath(config.getCdnRoot() + TEST_IMG);
    ASSERT(path == TEST_IMG);

    return true;
}

bool testMenuMatches()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    MenuItem item = MenuItem(TEST_STR, TEST_STR, TEST_STR, TEST_STR, TEST_NUM, TEST_BOOL, &config, TEST_NUM);
    ASSERT(item.matches(TEST_STR));
    ASSERT(!item.matches(TEST_FAIL_FILTER));
    ASSERT(item.matches(std::to_string(TEST_NUM)));

    FilteredList<MenuItem> f;

    return true;
}

bool testFilteredList()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    MenuItem item = MenuItem(TEST_STR, TEST_STR, TEST_STR, TEST_STR, TEST_NUM, TEST_BOOL, &config, TEST_NUM);
    std::list<MenuItem> menu;
    menu.push_back(item);

    FilteredList<MenuItem> f = FilteredList(menu);
    ASSERT(f.size() == menu.size());
    ASSERT(f.getFiltered().front().getName() == TEST_STR);

    f.filter(TEST_FAIL_FILTER);
    ASSERT(f.size() == 0);

    std::list<MenuItem> menu2;
    f.setBase(menu2);
    ASSERT(f.size() == menu2.size());

    MenuItem item2 = MenuItem(TEST_FAIL_FILTER, TEST_STR, TEST_STR, TEST_STR, TEST_NUM, TEST_BOOL, &config, TEST_NUM);
    menu2.push_back(item2);
    f.setBase(menu2);
    ASSERT(f.size() == menu2.size());
    ASSERT(f.getFiltered().front().getName() == TEST_FAIL_FILTER);

    return true;
}

bool testInvItemMatches()
{
    InventoryItem item = InventoryItem(TEST_STR, TEST_STR, TEST_NUM);
    ASSERT(item.matches(TEST_STR));
    ASSERT(!item.matches(TEST_FAIL_FILTER));
    ASSERT(item.matches(std::to_string(TEST_NUM)));

    FilteredList<InventoryItem> f;

    return true;
}

bool testGetInventoryItems()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    std::list<InventoryItem> items = config.getInvItems();
    ASSERT(items.size() > 0);

    return true;
}

bool testGetIventoryItemsThatUse()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    std::list<InventoryItem> items = config.getInvItems();
    ASSERT(items.size() > 0);

    std::list<MenuItem> used = config.getItemsThatUse(items.front());
    ASSERT(used.size() > 0);

    return true;
}

bool testInStock()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    std::list<MenuItem> menu = config.getMenu();
    ASSERT(menu.size() > 0);

    for (MenuItem item : menu) {
        if (item.getName() == "not in stock") {
            ASSERT(!config.isInStock(item));
        } else if (item.getName() != TEST_STR) {
            ASSERT(config.isInStock(item));
        }
    }

    return true;
}

bool testInvItemUpdate()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    std::list<InventoryItem> items = config.getInvItems();
    ASSERT(items.size() > 0);

    InventoryItem orig = items.front();
    std::string uuid = orig.getUUID();

    InventoryItem newItem = InventoryItem(uuid, TEST_STR, TEST_NUM);
    ASSERT(config.updateInventoryItem(orig, newItem));

    items = config.getInvItems();
    bool found = false;

    for (InventoryItem item : items) {
        if (item.getUUID() == uuid) {
            ASSERT(item.getName() == TEST_STR);
            ASSERT(item.getAmount() == TEST_NUM);
            found = true;
            break;
        }
    }

    return found;
}

bool testAddItem()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    InventoryItem newitem = InventoryItem(TEST_STR_2, TEST_NUM);
    ASSERT(config.addInvItem(newitem));

    std::list<InventoryItem> items = config.getInvItems();
    bool found = false;

    for (InventoryItem item : items) {
        if (item.getUUID() == newitem.getUUID()) {
            ASSERT(item.getName() == TEST_STR_2);
            ASSERT(item.getAmount() == TEST_NUM);
            found = true;
            break;
        }
    }

    return found;
}

bool testGetInvMenuItemsFor()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    std::list<MenuItem> menu = config.getMenu();
    config.getInvItems(); // Needed to init the map

    ASSERT(menu.size() > 0);

    for (MenuItem item : menu) {
        if (item.getName() == "big chungus") {
            std::list<InventoryMenuItem> items = config.getInvMenuItemsFor(item);
            std::cout << items.size()
                      << std::endl;

            ASSERT(items.size() == 1);
            ASSERT(items.front().getName() == "chungus");
            ASSERT(items.front().getUnitsRequired() == 10);
        }
    }

    return true;
}

bool testUpdateInvMenuItems()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    std::list<MenuItem> menu = config.getMenu();
    std::list<InventoryItem> items = config.getInvItems();
    InventoryMenuItem invitem1;
    InventoryItem invitem2;

    ASSERT(menu.size() > 0);

    for (MenuItem item : menu) {
        if (item.getName() == TEST_INV_MENU_UPDATE) {
            std::list<InventoryMenuItem> items = config.getInvMenuItemsFor(item);
            ASSERT(items.size() > 0);
            invitem1 = items.front(); // This is updatedMenu

            // Get item to add which is not used by this item
            std::list<InventoryItem> invitems = config.getInvItems();
            for (InventoryItem invitem : invitems) {
                bool usedByItem = false;
                std::list<MenuItem> items = config.getItemsThatUse(invitem);
                for (MenuItem i : items) {
                    if (i.getUUID() == item.getUUID()) {
                        usedByItem = true;
                        break;
                    }
                }

                if (!usedByItem) {
                    invitem2 = invitem;
                    break;
                }
            }

            // Delete
            items = std::list<InventoryMenuItem>();

            // Update
            items.push_back(InventoryMenuItem(item.getUUID(),
                                              invitem1.getInvUUID(),
                                              invitem1.getName(),
                                              2,
                                              TEST_NUM));

            // Add
            items.push_back(InventoryMenuItem(item.getUUID(),
                                              invitem2.getUUID(),
                                              invitem2.getName(),
                                              2,
                                              TEST_NUM));

            config.updateInvMenuItems(item, items);
        }
    }

    config.getInvItems(); // Needed to update the map

    for (MenuItem item : menu) {
        if (item.getName() == TEST_INV_MENU_UPDATE) {
            std::list<InventoryMenuItem> items = config.getInvMenuItemsFor(item);
            ASSERT(items.size() == 2);

            bool found1 = false;
            bool found2 = false;

            for (InventoryMenuItem invmenuitem : items) {
                std::cerr << invmenuitem.getName()
                          << " | "
                          << invmenuitem.getUnitsRequired()
                          << " | "
                          << TEST_NUM
                          << std::endl;
                ASSERT(invmenuitem.getUnitsRequired() == TEST_NUM);

                found1 |= invmenuitem.getInvUUID() == invitem1.getInvUUID();
                found2 |= invmenuitem.getInvUUID() == invitem2.getUUID();
            }

            ASSERT(found1 && found2);
        }
    }

    return true;
}

bool testGetMenuTypes()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    std::list<MenuType> types = config.getMenuTypes();
    ASSERT(types.size() > 0);

    return true;
}

bool testGetItemsThatAre()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    std::list<MenuType> types = config.getMenuTypes();
    ASSERT(types.size() > 0);

    size_t total = 0;
    for (MenuType type : types) {
        std::list<MenuItem> usedBy = config.getItemsThatAre(type);
        total += usedBy.size() > 0;
    }
    ASSERT(total > 0);

    return true;
}

bool testUpdateMenuTypes()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    std::list<MenuType> types = config.getMenuTypes();
    ASSERT(types.size() > 0);

    MenuType old = types.front();
    MenuType newType = MenuType(old.getUUID(), TEST_STR, TEST_STR_2, TEST_STR_3);

    ASSERT(config.updateMenuType(old, newType));
    types = config.getMenuTypes();
    ASSERT(types.size() > 0);

    bool found = false;

    for (MenuType t : types) {
        if (t.getUUID() == old.getUUID()) {
            ASSERT(t.getName() == TEST_STR);
            ASSERT(t.getImage() == TEST_STR_2);
            ASSERT(t.getDesc() == TEST_STR_3);
            found = true;
            break;
        }
    }

    ASSERT(found);

    return true;
}

bool testAddMenuType()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    std::list<MenuType> types = config.getMenuTypes();
    MenuType old = MenuType(TEST_STR_DELETE, TEST_STR, TEST_STR_2);

    ASSERT(config.addMenuType(old));
    types = config.getMenuTypes();

    bool found = false;

    for (MenuType t : types) {
        if (t.getUUID() == old.getUUID()) {
            ASSERT(t.getName() == TEST_STR_DELETE);
            ASSERT(t.getImage() == TEST_STR);
            ASSERT(t.getDesc() == TEST_STR_2);
            found = true;
            break;
        }
    }

    ASSERT(found);

    return true;
}

bool testDeleteMenuType()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    std::list<MenuType> types = config.getMenuTypes();
    ASSERT(types.size() > 0);

    MenuType old;
    for (MenuType type : types) {
        if (type.getName() == TEST_STR_DELETE) {
            old = type;
            break;
        }
    }

    ASSERT(config.deleteMenuType(old));
    types = config.getMenuTypes();

    bool found = false;

    for (MenuType t : types) {
        if (t.getUUID() == old.getUUID()) {
            found = true;
            break;
        }
    }

    ASSERT(!found);

    return true;
}

bool testGetUsers()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    std::list<User> users = config.getUsers();
    ASSERT(users.size() > 0);

    return true;
}

bool testUpdateUser()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    std::list<User> users = config.getUsers();
    ASSERT(users.size() > 0);

    User oldUser;
    bool found = false;
    for (User user : users) {
        printf("User with id: %s and, Email: %s\n", user.getUUID().c_str(), user.getEmail().c_str());
        if (user.getEmail() == "test email") {
            oldUser = user;
            found = true;
            break;
        }
    }
    ASSERT(found);

    User newUser = User(oldUser.getUUID(),
                        TEST_STR,
                        TEST_STR_2,
                        "this should not change",
                        TEST_STR_3,
                        USER_KITCHEN);
    ASSERT(config.updateUser(oldUser, newUser));

    std::list<User> updated_users = config.getUsers();
    User testUser;
    found = false;
    for (User user : updated_users) {
        printf("User with id: %s and, Email: %s\n", user.getUUID().c_str(), user.getEmail().c_str());
        if (user.getEmail() == oldUser.getEmail()) {
            testUser = user;
            found = true;
            break;
        }
    }

    ASSERT(found);
    ASSERT(testUser.getUUID() == oldUser.getUUID());
    ASSERT(testUser.getFName() == TEST_STR);
    ASSERT(testUser.getSName() == TEST_STR_2);
    ASSERT(testUser.getPhoneNo() == TEST_STR_3);
    ASSERT(testUser.getUseType() == USER_KITCHEN);

    return true;
}

bool testGenSalt()
{
    std::string ret = getSalt();
    ASSERT(ret.size() == SALT_LENGTH);
    ASSERT(getSalt() != getSalt()); // What order do these get executed in ? idk lol
    return true;
}

bool testHashPassword()
{
    int s = 0;
    std::string salt = getSalt();
    std::string hash = hashPassword(TEST_STR, salt, &s);
    ASSERT(s);
    std::string hash2 = hashPassword(TEST_STR, salt, &s);
    ASSERT(s);

    ASSERT(hash == hash2);
    std::cerr << hash << std::endl;
    ASSERT(hash.size() == SHA512_DIGEST_STRING_LENGTH); // SHA 512 digest size

    return true;
}

bool testKnownHash()
{
    std::string password = "password";
    std::string salt = "salt";
    std::string knownHash = "fa6a2185b3e0a9a85ef41ffb67ef3c1fb6f74980f8ebf970e4e72e353ed9537d593083c201dfd6e43e1c8a7aac2bc8dbb119c7dfb7d4b8f131111395bd70e97f";

    int s = 0;
    std::string hash = hashPassword(password, salt, &s);
    ASSERT(s);
    ASSERT(hash == knownHash);


    std::string password2 = "noiqwawihd98whefsdfsdfsdfddsfsdfsdfewuifnskdaskda";
    std::string salt2 = "dnlkadnoashdiashdiup";
    std::string knownHash2 = "486818863bd4d7a23e5f08f69624594d4506200d7df305fc70049cae22b1c4b01c4032d8484a41d1112edfbeb1d414a2f9512ec1beca3faad755e5dc7b90e3d8";
    hash = hashPassword(password2, salt2, &s);

    ASSERT(s);
    ASSERT(hash == knownHash2);
    return true;
}

bool testAddUser()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    User newUser = User("Firstname", "Surname", "alishdasuhdg@gmail.com", "045989898989", USER_CUSTOMER);
    config.addUser(newUser, "password123123123123");

    std::list<User> users = config.getUsers();
    ASSERT(users.size() > 0);

    bool found = false;
    for (User user : users) {
        printf("User with id: %s and, Email: %s\n", user.getUUID().c_str(), user.getEmail().c_str());
        if (user.getUUID() == newUser.getUUID()) {
            found = true;
            break;
        }
    }
    ASSERT(found);

    return true;
}

bool testGetPassword()
{
    Configuration config;
    ASSERT(config.getDbName() == TEST_ENV);

    User newUser = User("Firstname", "Surname", "alishdasuhdg@gmail.com", "045989898989", USER_CUSTOMER);
    config.addUser(newUser, "password123123123123");

    std::list<User> users = config.getUsers();
    ASSERT(users.size() > 0);

    bool found = false;
    for (User user : users) {
        std::pair<std::string, std::string> passwordSalt = config.getUserPassword(user);
        ASSERT(passwordSalt.first != "");
        ASSERT(passwordSalt.second != "");
        break;
    }

    return true;
}

int test()
{
    // Delete the configuration file, ignoring failure
    remove(CONFIGURATION_FILE);

    int fails = 0;
    unit_test tests[] = {
        {&testConfigSave, "Confiugration::save"},
        {&testConfigSave, "Configuration::Configuration (load)"},
        {&testGetMenu, "Configuration::getMenu"},
        {&testGetMenuTypes, "Configuration::getMenuTypes"},
        {&testUpdateMenu, "Configuration::updateMenuItem"},
        {&testAddToMenu, "Configuration::addMenuItem"},
        {&testToCdnPathNoCopy, "Configuration::toCdnPath (no copy)"},
        {&testMenuMatches, "MenuItem::match"},
        {&testInvItemMatches, "InventoryItem::match"},
        {&testGetInventoryItems, "Configuration::getInvItems"},
        {&testGetIventoryItemsThatUse, "Configuration::getItemsThatUse"},
        {&testFilteredList, "FilteredList<T> tests"},
        {&testInStock, "Configuration::inStock"},
        {&testInvItemUpdate, "Configuration::updateInventoryItem"},
        {&testAddItem, "Configuration::addInvItem"},
        {&testGetInvMenuItemsFor, "Configuration::getInvMenuItemsFor"},
        {&testUpdateInvMenuItems, "Configuration::updateInvMenuItems"},
        {&testGetItemsThatAre, "Configuration::getItemsThatAre"},
        {&testAddMenuType, "Configuration::addMenuType"},
        {&testDeleteMenuType, "Configuration::deleteMenuType"},
        {&testGetUsers, "Configuration::getUsers"},
        {&testUpdateUser, "Configuration::updateUser"},
        {&testGenSalt, "getSalt"},
        {&testHashPassword, "hashPassword"},
        {&testKnownHash, "hash with known result"},
        {&testAddUser, "Configuration::addUser"}
    };

    run_tests(tests, sizeof(tests) / sizeof(*tests), "admin-console configuration.cpp");

    return fails == 0 ? 0 : 1;
}
