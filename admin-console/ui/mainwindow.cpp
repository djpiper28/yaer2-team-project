#include <QMessageBox>
#include <QGraphicsPixmapItem>
#include <QFileDialog>
#include <QUuid>
#include <iostream>
#include <string>
#include <pqxx/pqxx>
#include <stdlib.h>
#include "additemdialogue.h"
#include "menuitemtable.h"
#include "mainwindow.h"
#include "ui_mainwindow.h"
#include "addinvitemdialogue.h"
#include "editinventorymenu.h"
#include "addmenutypedialogue.h"
#include "changepassworddialogue.h"
#include "adduserdialogue.h"
#include "checkpassworddialogue.h"
#include "../discord_game_sdk.h"

MainWindow::MainWindow(QWidget *parent)
    : QMainWindow(parent)
    , ui(new Ui::MainWindow)
{
    ui->setupUi(this);
    ui->versionInfo->setText("**Version information: |** [Github Repo](" REPO_URL
                             ") **| OS:** " OS " **| VERSION:** " VERSION);
    this->config = Configuration();
    QIcon icon = QIcon("icon.png");
    this->setWindowIcon(icon);
    this->setWindowTitle(tr("Oxana Admin Console"));

    // Init config
    this->resetConfigButton();

    // Init menu editor tab
    this->menumdl = new MenuItemTable(this->config.getMenu(), &config, parent); // Inits the menu items
    this->menuItemSelectionModel = new QItemSelectionModel(this->menumdl);
    this->isMenuSelected = false;
    this->menuScene = new QGraphicsScene(parent);
    this->menuImg = nullptr;
    ui->imageView->setScene(this->menuScene);
    ui->menuItemTable->setModel(this->menumdl);
    ui->menuItemTable->setSelectionModel(this->menuItemSelectionModel);

    this->types = this->config.getMenuTypes();
    this->comboModel = new MenuTypeTable(this->types, &config, true, parent);
    ui->itemTypeEdit->setModel(this->comboModel);
    emit ui->itemTypeEdit->setCurrentIndex(0);

    // Init menu type editor tab
    this->menuTypeModel = new MenuTypeTable(this->config.getMenuTypes(), &config, parent);
    // ^ Inits the menu items
    this->menuTypedUsedByMdl = new MenuItemTable(std::list<MenuItem>(), &config, parent);
    this->menuTypeSelectionModel = new QItemSelectionModel(this->menuTypeModel);
    this->isMenuTypeSelected = false;
    this->menuTypeScene = new QGraphicsScene(parent);
    this->menuTypeImg = nullptr;
    ui->menuTypeImage->setScene(this->menuTypeScene);
    ui->menuTypeTableView->setModel(this->menuTypeModel);
    ui->menuTypeTableView->setSelectionModel(this->menuTypeSelectionModel);

    // Init inventory editor tab
    this->invmdl = new InventoryItemTable(this->config.getInvItems(), parent); // Inits the inv maps
    this->invItemSelectionModel = new QItemSelectionModel(this->invmdl);
    this->invItemUsesMdl = new MenuItemTable(std::list<MenuItem>(), &config, parent);
    this->isInvSelected = false;
    ui->invItemTable->setModel(this->invmdl);
    ui->invItemTable->setSelectionModel(this->invItemSelectionModel);

    // User editor tab
    this->isUserSelected = false;
    this->userTable = new UserTable(this->config.getUsers(), parent);
    this->userSelectionModel = new QItemSelectionModel(this->userTable);
    ui->userTableView->setModel(this->userTable);
    ui->userTableView->setSelectionModel(this->userSelectionModel);
    ui->userTypeEdit->insertItem(USER_CUSTOMER, tr("Customer"));
    ui->userTypeEdit->insertItem(USER_KITCHEN, tr("Kitchen"));
    ui->userTypeEdit->insertItem(USER_WAITER, tr("Waiter"));

    // Connect config tab
    connect(ui->resetConfig, &QPushButton::clicked, this, &MainWindow::resetConfigButton);
    connect(ui->applyConfigChanges, &QPushButton::clicked, this, &MainWindow::applyConfigChanges);
    connect(ui->cdnRootFolderBtn, &QPushButton::clicked, this, &MainWindow::setCdnRoot);

    // Connect menu editor tab
    connect(this->menuItemSelectionModel, &QItemSelectionModel::selectionChanged, this, &MainWindow::selectMenuItem);
    connect(ui->searchBar, &QLineEdit::textChanged, this, &MainWindow::filterMenu);
    connect(ui->saveChanges, &QPushButton::clicked, this, &MainWindow::saveMenuChanges);
    connect(ui->refreshButton, &QPushButton::clicked, this, &MainWindow::refreshMenu);
    connect(ui->addItem, &QPushButton::clicked, this, &MainWindow::addMenuItem);
    connect(ui->changeImage, &QPushButton::clicked, this, &MainWindow::setMenuImage);
    connect(ui->imageUriEdit, &QLineEdit::textChanged, this, &MainWindow::changeMenuImage);
    connect(ui->changeInvMenuItems, &QPushButton::clicked, this, &MainWindow::changeInvMenuItems);

    // Connect menu type editor tab
    connect(this->menuTypeSelectionModel, &QItemSelectionModel::selectionChanged, this, &MainWindow::selectMenuType);
    connect(ui->menuTypeRefresh, &QPushButton::clicked, this, &MainWindow::refreshMenuTypes);
    connect(ui->menuTypeSearch, &QLineEdit::textChanged, this, &MainWindow::filterMenuType);
    connect(ui->menuTypeChangeImage, &QPushButton::clicked, this, &MainWindow::setMenuTypeImage);
    connect(ui->menuTypeSave, &QPushButton::clicked, this, &MainWindow::saveMenuTypeChanges);
    connect(ui->menuTypeAdd, &QPushButton::clicked, this, &MainWindow::addMenuType);
    connect(ui->menuTypeDelete, &QPushButton::clicked, this, &MainWindow::deleteMenuType);

    // Connect inventory editor tab
    connect(this->invItemSelectionModel, &QItemSelectionModel::selectionChanged, this, &MainWindow::selectInvItem);
    connect(ui->invItemTableSearch, &QLineEdit::textChanged, this, &MainWindow::filterInv);
    connect(ui->invItemRefresh, &QPushButton::clicked, this, &MainWindow::refreshInv);
    connect(ui->invItemSave, &QPushButton::clicked, this, &MainWindow::saveInvChanges);
    connect(ui->invItemAddItem, &QPushButton::clicked, this, &MainWindow::addInvItem);

    // Connect all image resizers
    connect(&this->resizeTimer, &QTimer::timeout, this, &MainWindow::resizeImage);
    emit this->resizeImage();

    // Connect user editor
    connect(this->userSelectionModel, &QItemSelectionModel::selectionChanged, this, &MainWindow::selectUser);
    connect(ui->userSearch, &QLineEdit::textChanged, this, &MainWindow::filterUsers);
    connect(ui->userRefresh, &QPushButton::clicked, this, &MainWindow::refreshUsers);
    connect(ui->userSaveChanges, &QPushButton::clicked, this, &MainWindow::saveUserChanges);
    connect(ui->userChangePassword, &QPushButton::clicked, this, &MainWindow::changePassword);
    connect(ui->userAdd, &QPushButton::clicked, this, &MainWindow::addUser);
    connect(ui->userCheckPassword, &QPushButton::clicked, this, &MainWindow::checkPassword);

    // Init the discord rich presence (I promise this feature is needed)
    memset(&app, 0, sizeof(app));

    IDiscordCoreEvents events;
    memset(&events, 0, sizeof(events));

    struct DiscordCreateParams params;
    params.client_id = CLIENT_ID;
    params.flags = DiscordCreateFlags_Default;
    params.events = &events;
    params.event_data = &app;

    DiscordCreate(DISCORD_VERSION, &params, &app.core);

    this->dcStartTime = time(NULL);

    connect(&this->discordTimer, &QTimer::timeout, this, &MainWindow::discordPoll);
    emit this->discordPoll();
}

MainWindow::~MainWindow()
{
    delete ui;
    delete menuItemSelectionModel;
    delete invItemSelectionModel;
    delete menumdl;
    delete invmdl;
    delete comboModel;
    delete menuTypeModel;
    delete menuTypedUsedByMdl;
    delete invItemUsesMdl;
    delete userTable;
}

//==========
// Utils
//==========

void MainWindow::refreshAll()
{
    emit this->refreshInv();
    emit this->refreshMenu();
    emit this->refreshUsers();
}

void MainWindow::refreshTypeEdit()
{
    this->types = this->config.getMenuTypes();
    this->comboModel->setMenuTypes(this->types);

    ui->itemTypeEdit->setModel(NULL);
    ui->itemTypeEdit->setModel(this->comboModel);
}

void MainWindow::resizeImage()
{
    // Menu item image
    if (this->isMenuSelected && this->menuImg != nullptr) {
        double w = this->menuImage.width();
        double h = this->menuImage.height();

        if (w == 0 || h == 0) {
            return;
        }

        double scale1 = ui->imageView->width() / w;
        double scale2 = ui->imageView->height() / h;

        // Min
        double scale = scale1;
        if (scale2 < scale1) {
            scale = scale2;
        }

        // Set scale
        this->menuImg->setScale(scale);
        this->menuImg->setPos(0, 0);
    }

    // Menu type image
    if (this->isMenuTypeSelected && this->menuTypeImg != nullptr) {
        double w = this->menuTypeImage.width();
        double h = this->menuTypeImage.height();

        if (w == 0 || h == 0) {
            return;
        }

        double scale1 = ui->menuTypeImage->width() / w;
        double scale2 = ui->menuTypeImage->height() / h;

        // Min
        double scale = scale1;
        if (scale2 < scale1) {
            scale = scale2;
        }

        // Set scale
        this->menuTypeImg->setScale(scale);
        this->menuTypeImg->setPos(0, 0);
    }

    this->resizeTimer.start(500);
}

void MainWindow::discordPoll()
{
    this->app.core->run_callbacks(this->app.core);
    size_t diff = time(NULL) - this->dcStartTime;
    size_t h = diff / 3600,
           m = ((diff / 60) % 60),
           s = diff % 60;
    std::string elapsedStr = (h < 10 ? "0" : "") + std::to_string(h) + ":";
    elapsedStr += (m < 10 ? "0" : "") + std::to_string(m) + ":";
    elapsedStr += (s < 10 ? "0" : "") + std::to_string(s);
    std::string info = "(" + ui->tabWidget->tabText(ui->tabWidget->currentIndex()).toStdString() + ")";

    DiscordActivity activity;
    memset(&activity, 0, sizeof(activity));
    activity.type = DiscordActivityType_Playing;
    strncpy(activity.name, tr("Editing Oxana Menu").toStdString().c_str(), 128);
    strncpy(activity.details, (elapsedStr + " " + tr("Elapsed").toStdString()).c_str(), 128);
    strncpy(activity.state, (info).c_str(), 128);
    strncpy(activity.assets.large_image, DISCORD_LARGE_IMG, 128);

    IDiscordActivityManager *act = this->app.core->get_activity_manager(this->app.core);
    act->update_activity(act, &activity, NULL, NULL);

    this->discordTimer.start(1000);
}

// ==========
// Configuration tab
// ==========

void MainWindow::resetConfigButton()
{
    ui->cdnRootEdit->setText(QString::fromStdString(this->config.getCdnRoot()));
    ui->psqlDbEdit->setText(QString::fromStdString(this->config.getDbName()));
    ui->psqlUrlEdit->setText(QString::fromStdString(this->config.getDbHostAddress()));
    ui->psqlUseNameEdit->setText(QString::fromStdString(this->config.getDbUserName()));
    ui->psqlPasswordEdit->setText(QString::fromStdString(this->config.getDbPassword()));
    ui->psqlPortEdit->setValue(this->config.getPort());
}

void MainWindow::applyConfigChanges()
{
    this->config.setCdnRoot(ui->cdnRootEdit->text().toStdString());
    this->config.setDbName(ui->psqlDbEdit->text().toStdString());
    this->config.setDbUserName(ui->psqlUseNameEdit->text().toStdString());
    this->config.setDbHostAddress(ui->psqlUrlEdit->text().toStdString());
    this->config.setDbPassword(ui->psqlPasswordEdit->text().toStdString());
    this->config.setPort(ui->psqlPortEdit->value());

    this->config.save();
    emit this->refreshAll();
}

void MainWindow::setCdnRoot()
{
    QFileDialog dialog(this, tr("Select the cdn root folder (TeamProject2022_05/cdn)"));
    dialog.setFileMode(QFileDialog::Directory);
    dialog.setViewMode(QFileDialog::Detail);
    if (dialog.exec()) {
        ui->cdnRootEdit->setText(dialog.directory().absolutePath());
    }
}

// ==========
// Menu editor
// ==========

void MainWindow::selectMenuItem(const QItemSelection &selected, const QItemSelection deselected)
{
    MenuItem item = this->menumdl->getItem(ui->menuItemTable->currentIndex().row());

    ui->nameEdit->setText(QString::fromStdString(item.getName()));
    ui->descEdit->setText(QString::fromStdString(item.getDescription()));
    ui->priceEdit->setValue(item.getPrice());
    ui->imageUriEdit->setText(QString::fromStdString(item.getImageURI()));
    ui->activeEdit->setChecked(item.isActive());
    ui->inStockIndicator->setChecked(item.inStock());
    ui->prepTimeEdit->setValue(item.getPrepTime());
    int index = 0;
    int i = 0;
    for (MenuType type : this->types) {
        if (type.getUUID() == item.getTypeUUID()) {
            index = i;
            break;
        }

        i++;
    }

    emit ui->itemTypeEdit->setCurrentIndex(index);

    this->selectedMenuItem = item;
    this->isMenuSelected = true;

    emit this->changeMenuImage();
}

void MainWindow::setMenuImage()
{
    QFileDialog dialog = QFileDialog(this, tr("Select the image for the item"));
    dialog.setFileMode(QFileDialog::ExistingFile);
    dialog.setNameFilter(tr("Images (*.png *.xpm *.jpg *.jpeg *.webp)"));
    dialog.setViewMode(QFileDialog::Detail);

    if (!dialog.exec()) {
        return;
    }

    if (dialog.selectedFiles().size() != 1) {
        return;
    }

    std::string newImagePath = this->config.toCdnPath(dialog.selectedFiles().at(0).toStdString());
    if (newImagePath == "") {
        return;
    }

    ui->imageUriEdit->setText(QString::fromStdString(newImagePath));
    emit this->changeMenuImage();
    std::cout << "A new image was selected "
              << newImagePath
              << std::endl;
}

void MainWindow::changeMenuImage()
{
    if (this->isMenuSelected) {
        std::string uri = this->config.getCdnRoot() + "/" + ui->imageUriEdit->text().toStdString();
        std::cout << "Loading image "
                  << uri
                  << std::endl;
        FILE *f = fopen(uri.c_str(), "rb");
        if (f == NULL) {
            std::cerr << "Image is not present"
                      << std::endl;
        } else {
            fclose(f);
        }

        if (this->menuImg != nullptr) {
            this->menuScene->removeItem(this->menuImg);

            delete this->menuImg;
        }
        this->menuImage = QImage(QString::fromStdString(uri));
        this->menuImg = new QGraphicsPixmapItem(QPixmap::fromImage(menuImage));
        this->menuScene->addItem(this->menuImg);
        this->menuImg->setPos(0, 0);

        ui->imageView->show();
        emit this->resizeImage();
    } else {
        std::cerr << "Cannot show an empty item"
                  << std::endl;
    }
}

void MainWindow::refreshMenu()
{
    std::list<MenuItem> menu = this->config.getMenu();
    this->menumdl->setMenu(menu);
    ui->menuItemTable->setModel(NULL);
    ui->menuItemTable->setModel(this->menumdl);
    ui->menuItemTable->setSelectionModel(this->menuItemSelectionModel);
    std::cerr << "Updated the menu"
              << std::endl;

    // These are based off the menu data
    emit this->refreshTypeEdit();
    emit this->refreshMenuTypes();
}

void MainWindow::saveMenuChanges()
{
    // If there is no selected beep and return
    if (!this->isMenuSelected) {
        QApplication::beep();
        std::cout << "No item is selected"
                  << std::endl;
        return;
    } else {
        bool valid = this->config.isInputValid(ui->nameEdit->text().toStdString())
                     && this->config.isInputValid(ui->descEdit->toPlainText().toStdString())
                     && this->config.isInputValid(ui->imageUriEdit->text().toStdString());
        if (!valid) {
            QApplication::beep();
            std::cout << "Invalid input"
                      << std::endl;
        } else {
            MenuType type;
            int i = 0;
            for (MenuType t : this->types) {
                if (i == ui->itemTypeEdit->currentIndex()) {
                    type = t;
                    break;
                }

                i++;
            }

            MenuItem item(ui->nameEdit->text().toStdString(),
                          ui->descEdit->toPlainText().toStdString(),
                          ui->imageUriEdit->text().toStdString(),
                          this->selectedMenuItem.getUUID(),
                          type.getUUID(),
                          ui->priceEdit->value(),
                          ui->activeEdit->isChecked(),
                          &this->config,
                          ui->prepTimeEdit->value());

            if (this->config.updateMenuItem(this->selectedMenuItem, item)) {
                emit this->refreshAll();

                QMessageBox msgBox;
                msgBox.setText(tr("The menu was updated."));
                msgBox.exec();
            } else {
                QApplication::beep();
                std::cout << "Failed to update menu item"
                          << std::endl;

                QMessageBox msgBox;
                msgBox.setText(tr("There was an error, please try again."));
                msgBox.exec();
            }
        }
    }
}

void MainWindow::addMenuItem()
{
    AddItemDialogue *addItemDialogue = new AddItemDialogue(&this->config, this);
    connect(addItemDialogue, &AddItemDialogue::onAdd, this, &MainWindow::refreshAll);
    addItemDialogue->show();
}

void MainWindow::filterMenu(QString text)
{
    this->menumdl->filter(text.toStdString());
    ui->menuItemTable->setModel(NULL);
    ui->menuItemTable->setModel(this->menumdl);
    ui->menuItemTable->setSelectionModel(this->menuItemSelectionModel);
}

void MainWindow::changeInvMenuItems()
{
    if (this->isMenuSelected) {
        EditInventoryMenu editInvMenu = EditInventoryMenu(&this->config,
                                        this->selectedMenuItem);
        connect(&editInvMenu, &EditInventoryMenu::onComplete, this, &MainWindow::refreshAll);
        editInvMenu.exec();
    } else {
        QApplication::beep();
    }
}

// ==========
// Menu Type editor
// ==========

void MainWindow::selectMenuType(const QItemSelection &selected, const QItemSelection deselected)
{
    MenuType item = this->menuTypeModel->getItem(ui->menuTypeTableView->currentIndex().row());

    ui->menuTypeName->setText(QString::fromStdString(item.getName()));
    ui->menuTypeImageUri->setText(QString::fromStdString(item.getImage()));
    ui->menuDescEdit->setText(QString::fromStdString(item.getDesc()));

    this->menuTypedUsedByMdl->setMenu(this->config.getItemsThatAre(item));
    ui->menuTypeUsedBy->setModel(NULL);
    ui->menuTypeUsedBy->setModel(this->menuTypedUsedByMdl);
    this->selectedMenuType= item;
    this->isMenuTypeSelected = true;

    emit this->changeMenuTypeImage();
}

void MainWindow::changeMenuTypeImage()
{
    if (this->isMenuTypeSelected) {
        std::string uri = this->config.getCdnRoot() + "/" + ui->menuTypeImageUri->text().toStdString();
        std::cout << "Loading image "
                  << uri
                  << std::endl;
        FILE *f = fopen(uri.c_str(), "rb");
        if (f == NULL) {
            std::cerr << "Image is not present"
                      << std::endl;
        } else {
            fclose(f);
        }

        if (this->menuTypeImg != nullptr) {
            this->menuTypeScene->removeItem(this->menuTypeImg);

            delete this->menuTypeImg;
        }
        this->menuTypeImage = QImage(QString::fromStdString(uri));
        this->menuTypeImg = new QGraphicsPixmapItem(QPixmap::fromImage(menuTypeImage));
        this->menuTypeScene->addItem(this->menuTypeImg);
        this->menuTypeImg->setPos(0, 0);

        ui->menuTypeImage->show();
        emit this->resizeImage();
    } else {
        std::cerr << "Cannot show an empty item"
                  << std::endl;
    }
}

void MainWindow::refreshMenuTypes()
{
    std::list<MenuType> types = this->config.getMenuTypes();
    this->menuTypeModel->setMenuTypes(types);
    ui->menuTypeTableView->setModel(NULL);
    ui->menuTypeTableView->setModel(this->menuTypeModel);
    ui->menuTypeTableView->setSelectionModel(this->menuTypeSelectionModel);
}

void MainWindow::filterMenuType(QString text)
{
    this->menuTypeModel->filter(text.toStdString());
    ui->menuTypeTableView->setModel(NULL);
    ui->menuTypeTableView->setModel(this->menuTypeModel);
    ui->menuTypeTableView->setSelectionModel(this->menuTypeSelectionModel);
}

void MainWindow::setMenuTypeImage()
{
    QFileDialog dialog = QFileDialog(this, tr("Select the image for the menu type"));
    dialog.setFileMode(QFileDialog::ExistingFile);
    dialog.setNameFilter(tr("Images (*.png *.xpm *.jpg *.jpeg *.webp)"));
    dialog.setViewMode(QFileDialog::Detail);

    if (!dialog.exec()) {
        return;
    }

    if (dialog.selectedFiles().size() != 1) {
        return;
    }

    std::string newImagePath = this->config.toCdnPath(dialog.selectedFiles().at(0).toStdString());
    if (newImagePath == "") {
        return;
    }

    ui->menuTypeImageUri->setText(QString::fromStdString(newImagePath));
    emit this->changeMenuTypeImage();
    std::cout << "A new image was selected "
              << newImagePath
              << std::endl;
}

void MainWindow::saveMenuTypeChanges()
{
    // If there is no selected beep and return
    if (!this->isMenuTypeSelected) {
        QApplication::beep();
        std::cout << "No type is selected"
                  << std::endl;
        return;
    } else {
        bool valid = this->config.isInputValid(ui->menuTypeName->text().toStdString())
                     && this->config.isInputValid(ui->menuTypeImageUri->text().toStdString())
                     && this->config.isInputValid(ui->menuDescEdit->toPlainText().toStdString());
        if (!valid) {
            QApplication::beep();
            std::cout << "Invalid input"
                      << std::endl;
        } else {
            MenuType type = MenuType(this->selectedMenuType.getUUID(),
                                     ui->menuTypeName->text().toStdString(),
                                     ui->menuTypeImageUri->text().toStdString(),
                                     ui->menuDescEdit->toPlainText().toStdString());

            if (this->config.updateMenuType(this->selectedMenuType, type)) {
                emit this->refreshAll();

                QMessageBox msgBox;
                msgBox.setText(tr("The menu type was updated."));
                msgBox.exec();
            } else {
                QApplication::beep();
                std::cout << "Failed to update menu type"
                          << std::endl;

                QMessageBox msgBox;
                msgBox.setText(tr("There was an error, please try again."));
                msgBox.exec();
            }
        }
    }
}

void MainWindow::deleteMenuType()
{
    // If there is no selected beep and return
    if (!this->isMenuTypeSelected) {
        QApplication::beep();
        std::cout << "No type is selected"
                  << std::endl;
        return;
    } else {
        if (this->config.deleteMenuType(this->selectedMenuType)) {
            emit this->refreshAll();

            QMessageBox msgBox;
            msgBox.setText(tr("The menu type was deleted."));
            msgBox.exec();
        } else {
            QApplication::beep();
            std::cout << "Failed to delete menu type"
                      << std::endl;

            QMessageBox msgBox;
            msgBox.setText(tr("There was an error deleting the type, maybe it is in use. Please check and try again."));
            msgBox.exec();
        }
    }
}

void MainWindow::addMenuType()
{
    AddMenuTypeDialogue *addItemDialogue = new AddMenuTypeDialogue(&this->config, this);
    connect(addItemDialogue, &AddMenuTypeDialogue::onAdd, this, &MainWindow::refreshAll);
    addItemDialogue->show();
}

// ==========
// Inventory editor
// ==========

void MainWindow::refreshInv()
{
    std::list<InventoryItem> items = this->config.getInvItems();
    this->invmdl->setItems(items);
    ui->invItemTable->setModel(NULL);
    ui->invItemTable->setModel(this->invmdl);
    ui->invItemTable->setSelectionModel(this->invItemSelectionModel);
    std::cerr << "Updated the inventory"
              << std::endl;

}

void MainWindow::selectInvItem(const QItemSelection &selected, const QItemSelection deselected)
{
    InventoryItem item = this->invmdl->getItem(ui->invItemTable->currentIndex().row());

    ui->invItemNameEdit->setText(QString::fromStdString(item.getName()));
    ui->invItemStockEdit->setValue(item.getAmount());

    this->selectedInvItem = item;
    this->isInvSelected = true;
    std::list<MenuItem> usedIn = this->config.getItemsThatUse(item);
    this->invItemUsesMdl->setMenu(usedIn);

    ui->invItemUsedInTable->setModel(NULL);
    ui->invItemUsedInTable->setModel(this->invItemUsesMdl);
}

void MainWindow::filterInv(QString text)
{
    this->invmdl->filter(text.toStdString());
    ui->invItemTable->setModel(NULL);
    ui->invItemTable->setModel(this->invmdl);
    ui->invItemTable->setSelectionModel(this->invItemSelectionModel);
}

void MainWindow::saveInvChanges()
{
    if (this->isInvSelected) {
        bool valid = ui->invItemStockEdit->value() >= 0
                     && config.isInputValid(ui->invItemNameEdit->text().toStdString());

        if (valid) {
            InventoryItem newItem = InventoryItem(this->selectedInvItem.getUUID(),
                                                  ui->invItemNameEdit->text().toStdString(),
                                                  ui->invItemStockEdit->value());
            if (this->config.updateInventoryItem(this->selectedInvItem, newItem)) {
                QMessageBox msgBox;
                msgBox.setText(tr("Inventory updated."));
                msgBox.exec();

                emit this->refreshAll();
            } else {
                QApplication::beep();
                std::cout << "Failed to update inventory item"
                          << std::endl;

                QMessageBox msgBox;
                msgBox.setText(tr("There was an error, please try again."));
                msgBox.exec();
            }
        } else {
            QApplication::beep();
        }
    }
}

void MainWindow::addInvItem()
{
    AddInvItemDialogue *addItemDialogue = new AddInvItemDialogue(&this->config, this);
    connect(addItemDialogue, &AddInvItemDialogue::onAdd, this, &MainWindow::refreshAll);
    addItemDialogue->show();
}

// ==========
// User Editor
// ==========
void MainWindow::selectUser(const QItemSelection &selected, const QItemSelection deselected)
{
    User user = this->userTable->getUser(ui->userTableView->currentIndex().row());
    this->isUserSelected = true;
    this->selectedUser = user;

    ui->userEmailEdit->setText(QString::fromStdString(user.getEmail()));
    ui->userFnameEdit->setText(QString::fromStdString(user.getFName()));
    ui->userSnameEdit->setText(QString::fromStdString(user.getSName()));
    ui->userPhoneNoEdit->setText(QString::fromStdString(user.getPhoneNo()));
    ui->userTypeEdit->setCurrentIndex(user.getUseType());
}

void MainWindow::filterUsers(QString text)
{
    this->userTable->filter(text.toStdString());

    ui->userTableView->setModel(NULL);
    ui->userTableView->setModel(this->userTable);
    ui->userTableView->setSelectionModel(this->userSelectionModel);
}

void MainWindow::refreshUsers()
{
    this->userTable->setUsers(this->config.getUsers());

    ui->userTableView->setModel(NULL);
    ui->userTableView->setModel(this->userTable);
    ui->userTableView->setSelectionModel(this->userSelectionModel);
}

void MainWindow::saveUserChanges()
{
    if (this->isUserSelected) {
        bool valid = this->config.isInputValid(ui->userFnameEdit->text().toStdString())
                     && this->config.isInputValid(ui->userSnameEdit->text().toStdString())
                     && this->config.isInputValid(ui->userPhoneNoEdit->text().toStdString())
                     && 0 != atol(ui->userPhoneNoEdit->text().toStdString().c_str());

        if (valid) {
            User newUser = User(this->selectedUser.getUUID(),
                                ui->userFnameEdit->text().toStdString(),
                                ui->userSnameEdit->text().toStdString(),
                                this->selectedUser.getEmail(),
                                ui->userPhoneNoEdit->text().toStdString(),
                                ui->userTypeEdit->currentIndex());
            if (this->config.updateUser(this->selectedUser, newUser)) {
                emit this->refreshAll();

                QMessageBox msgBox;
                msgBox.setText(tr("The user was updated."));
                msgBox.exec();
            } else {
                QMessageBox msgBox;
                msgBox.setText(tr("Error could not update the user."));
                msgBox.exec();
            }
        } else {
            std::cerr << "Invalid Input" << std::endl;
            QApplication::beep();
        }
    } else {
        QApplication::beep();
    }
}

void MainWindow::changePassword()
{
    if (this->isUserSelected) {
        ChangePasswordDialogue *dialogue = new ChangePasswordDialogue(this->selectedUser, &this->config, this);
        dialogue->show();
    } else {
        QApplication::beep();
    }
}

void MainWindow::addUser()
{
    AddUserDialogue *addUserDialogue = new AddUserDialogue(&this->config, this);
    connect(addUserDialogue, &AddUserDialogue::onAdd, this, &MainWindow::refreshAll);
    addUserDialogue->show();
}

void MainWindow::checkPassword()
{
    if (this->isUserSelected) {
        std::pair<std::string, std::string> passwordSalt = this->config.getUserPassword(this->selectedUser);
        if (passwordSalt.first == "" || passwordSalt.second == "") {
            QMessageBox msgBox;
            msgBox.setText(tr("Error could not fetch user login details."));
            msgBox.exec();
        } else {
            CheckPasswordDialogue *dialogue = new CheckPasswordDialogue(this->selectedUser, passwordSalt.first, passwordSalt.second, this);
            dialogue->show();
        }
    } else {
        QApplication::beep();
    }
}
