#pragma once
#include <QDialog>
#include <QItemSelectionModel>
#include <list>
#include "../model/menuitem.h"
#include "../model/configuration.h"
#include "../model/inventoryitem.h"
#include "filteredlist.h"
#include "invitemtable.h"
#include "../model/inventorymenuitem.h"
#include "invmenuitemtable.h"

namespace Ui
{
class EditInventoryMenu;
}

class EditInventoryMenu : public QDialog
{
    Q_OBJECT

signals:
    void onComplete();
public:
    explicit EditInventoryMenu(Configuration *config, MenuItem item, QWidget *parent = nullptr);
    ~EditInventoryMenu();
    void removeInvItem(InventoryMenuItem item);
private slots:
    // right pane
    void addInvItem();
    void complete();
    void filterInvItems(QString query);
    void selectInvItem(const QItemSelection &selected, const QItemSelection deselected);

    // left pane
    void decInvItem();
    void requiredUnitsOverride(int value);
    void filterMenuInvItems(QString query);
    void selectMenuInvItem(const QItemSelection &selected, const QItemSelection deselected);
    void removeInvItemBtn();
private:
    Ui::EditInventoryMenu *ui;
    Configuration *config;
    MenuItem menuItem;
    std::list<InventoryMenuItem> invMenuItemsEdit;
    int lastSpinValue;
    bool waitingForSave;

    // Inv menu item table
    bool isMenuInvSelected;
    InventoryMenuItem selectedMenuInvItem;
    InventoryMenuItemTable *invMenuItems;
    QItemSelectionModel *invMenuItemSelectionModel;

    // Inv item table
    bool isInvSelected;
    InventoryItem selectedInvItem;
    InventoryItemTable *invItems;
    QItemSelectionModel *invItemSelectionModel;
};
