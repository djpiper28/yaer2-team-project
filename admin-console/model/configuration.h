#pragma once
#include <QObject>
#include <string>
#include <list>
#include <map>
#include <pqxx/pqxx>
#include "menuitem.h"
#include "inventoryitem.h"
#include "inventorymenuitem.h"
#include "menutype.h"
#include "user.h"

#define CONFIGURATION_FILE "admin-console-config.json"
#define CONFIG_DB_NAME "db_name"
#define CONFIG_DB_USERNAME "db_username"
#define CONFIG_DB_PASSWORD "db_password"
#define CONFIG_DB_PORT "db_port"
#define CONFIG_DB_HOST "db_host"
#define CONFIG_CDN_ROOT "cdn_root"

// Menu Items
#define MENU_ITEM_UPDATE_STATEMENT "update_menu_items"
#define MENU_ITEM_ADD_STATEMENT "add_menu_item"
#define INV_IN_STOCK_STATEMENT "get_inv_menus_in_stock"

// Inv items
#define INV_GET_USED_BY_STATEMENT "get_inv_used_by"
#define INV_ITEM_UPDATE_STATEMENT "update_inv_item"
#define INV_ADD_ITEM_STATEMENT "add_inv_item"

// Inv menu items
#define INV_MENU_GET_STATEMENT "get_inv_menu"
#define INV_MENU_ADD_STATEMTNT "add_inv_menu"
#define INV_MENU_UPDATE_STATEMNT "update_inv_menu"
#define INV_MENU_DELETE_STATEMNT "delete_inv_menu"

// Inv menu types
#define MENU_TYPE_ADD_STATEMNT "add_menu_type"
#define MENU_TYPE_UPDATE_STATEMNT "update_menu_type"
#define MENU_TYPE_DELETE_STATEMENT "delete_menu_type"

// Users
#define USER_UPDATE_STATEMENT "update_user"
#define USER_ADD_STATEMENT "add_user"
#define USER_CHANGE_PASSWORD "change_pwd_user"
#define USER_GET_PASSWORD "get_pwd_user"

// Read buffer consts
#define BUFFER_INC 4096;

// See gen_valid.py if you want to remake this without forgetting a letter
#define VALID_INPUT "012456789-_=+aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ"

class Configuration
{
public:
    Configuration();
    ~Configuration();

    // Database queries
    std::list<MenuItem> getMenu();
    std::list<InventoryItem> getInvItems();
    std::list<MenuItem> getItemsThatUse(InventoryItem item);
    bool isInStock(MenuItem item);
    std::list<User> getUsers();

    pqxx::connection *getConnection(); // Exposed for testing
    // Configuration settings
    std::string getCdnRoot();
    std::string getDbName();
    std::string getDbPassword();
    std::string getDbUserName();
    std::string getDbHostAddress();
    int getPort();

    void setDbName(std::string val);
    void setDbUserName(std::string val);
    void setDbPassword(std::string val);
    void setDbHostAddress(std::string val);
    void setCdnRoot(std::string val);
    void setPort(int port);
    // Validation
    bool isInputValid(std::string input);
    // Update
    bool updateMenuItem(MenuItem oldItem, MenuItem newItem);
    bool updateInventoryItem(InventoryItem oldItem, InventoryItem newItem);
    bool updateMenuType(MenuType oldType, MenuType newType);
    bool updateUser(User oldUser, User newUser);
    bool updateUserPassword(User user, std::string password);
    // Add
    bool addMenuItem(MenuItem item);
    bool addInvItem(InventoryItem item);
    bool addMenuType(MenuType type);
    bool addUser(User user, std::string password);
    // Delete
    bool deleteMenuType(MenuType type);
    // Get
    std::list<InventoryMenuItem> getInvMenuItemsFor(MenuItem item);
    bool updateInvMenuItems(MenuItem menuitem, std::list<InventoryMenuItem> invItems);
    std::list<MenuType> getMenuTypes();
    std::list<MenuItem> getItemsThatAre(MenuType type);
    std::pair<std::string, std::string> getUserPassword(User user);
    // File utils
    std::string toCdnPath(std::string filename);
    bool save();
private:
    std::string getDbConnString();
    std::map<std::string /*menuid*/, std::list<InventoryMenuItem>> menuIdToInvMenuItemMap;
    std::map<std::string /*invid*/, std::list<MenuItem>> invIdToItemMap; // Used to make getItemsThatUse cached
    std::map<std::string /*menu_type_id*/, std::list<MenuItem>> menuTypeIdToItemMap; // Used to make getItemsThatAre
    std::string db_name, db_username, db_password, db_host_address, cdn_root;
    int db_port;
    pqxx::connection *conn;
};
