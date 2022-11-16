#pragma once
#include <QMainWindow>
#include <QPushButton>
#include <QItemSelectionModel>
#include <QGraphicsScene>
#include <QTimer>
#include "menuitemtable.h"
#include "invitemtable.h"
#include "menutypetable.h"
#include "usertable.h"

QT_BEGIN_NAMESPACE
namespace Ui
{
class MainWindow;
}
QT_END_NAMESPACE

// Discord rich presence
#define CLIENT_ID 944251340882853889
#define DISCORD_LARGE_IMG "o"
#define WINDOW_ICON_NAME "icon.png"

struct Application {
    struct IDiscordCore* core;
    struct IDiscordUsers* users;
};

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    MainWindow(QWidget *parent = nullptr);
    ~MainWindow();
private slots:
    void refreshAll();

    // Image resize
    void resizeImage();

    // Config
    void resetConfigButton();
    void applyConfigChanges();
    void setCdnRoot();

    // Menu editor
    void selectMenuItem(const QItemSelection &selected, const QItemSelection deselected);
    void refreshMenu();
    void saveMenuChanges();
    void addMenuItem();
    void filterMenu(QString text);
    void setMenuImage();
    void changeMenuImage();
    void changeInvMenuItems();
    void refreshTypeEdit();

    // Menu type editor
    void selectMenuType(const QItemSelection &selected, const QItemSelection deselected);
    void refreshMenuTypes();
    void saveMenuTypeChanges();
    void addMenuType();
    void deleteMenuType();
    void filterMenuType(QString text);
    void setMenuTypeImage();
    void changeMenuTypeImage();

    // Inv editor
    void selectInvItem(const QItemSelection &selected, const QItemSelection deselected);
    void refreshInv();
    void filterInv(QString text);
    void saveInvChanges();
    void addInvItem();

    // Users
    void selectUser(const QItemSelection &selected, const QItemSelection deselected);
    void refreshUsers();
    void filterUsers(QString text);
    void saveUserChanges();
    void addUser();
    void changePassword();
    void checkPassword();

    // Discord
    void discordPoll();
private:
    Ui::MainWindow *ui;
    Configuration config;
    std::list<MenuType> types;
    QTimer resizeTimer;

    // Discord status
    struct Application app;
    size_t dcStartTime;
    QTimer discordTimer;

    // Menu
    MenuItem selectedMenuItem;
    bool isMenuSelected;
    MenuItemTable *menumdl;
    QItemSelectionModel *menuItemSelectionModel;
    QGraphicsPixmapItem *menuImg;
    QGraphicsScene* menuScene;
    QImage menuImage;
    MenuTypeTable *comboModel;

    // Menu Type editor
    MenuTypeTable *menuTypeModel;
    QItemSelectionModel *menuTypeSelectionModel;
    MenuType selectedMenuType;
    bool isMenuTypeSelected;
    MenuItemTable *menuTypedUsedByMdl;
    QGraphicsPixmapItem *menuTypeImg;
    QGraphicsScene* menuTypeScene;
    QImage menuTypeImage;

    // Inventory item
    InventoryItem selectedInvItem;
    bool isInvSelected;
    InventoryItemTable *invmdl;
    QItemSelectionModel *invItemSelectionModel;
    MenuItemTable *invItemUsesMdl;

    // Users
    User selectedUser;
    bool isUserSelected;
    UserTable *userTable;
    QItemSelectionModel *userSelectionModel;
};
