#include <QMessageBox>
#include <iostream>
#include "editinventorymenu.h"
#include "ui_editinventorymenu.h"

EditInventoryMenu::EditInventoryMenu(Configuration *config, MenuItem item, QWidget *parent) :
    QDialog(parent),
    ui(new Ui::EditInventoryMenu)
{
    ui->setupUi(this);
    this->setWindowTitle(tr("Edit Menu Item Components"));

    this->config = config;
    this->menuItem = item;
    this->waitingForSave = false;
    this->invMenuItemsEdit = config->getInvMenuItemsFor(this->menuItem);
    connect(ui->buttonBox, &QDialogButtonBox::accepted, this, &EditInventoryMenu::complete);

    // Init inventory item table
    this->invItems = new InventoryItemTable(this->config->getInvItems(), this);
    this->invItemSelectionModel = new QItemSelectionModel(this->invItems, this);
    this->isInvSelected = false;
    ui->invItems->setModel(this->invItems);
    ui->invItems->setSelectionModel(this->invItemSelectionModel);

    connect(this->invItemSelectionModel, &QItemSelectionModel::selectionChanged, this, &EditInventoryMenu::selectInvItem);
    connect(ui->invItemsSearch, &QLineEdit::textChanged, this, &EditInventoryMenu::filterInvItems);
    connect(ui->invItemsAdd, &QPushButton::clicked, this, &EditInventoryMenu::addInvItem);

    // Init menu items table
    this->invMenuItems = new InventoryMenuItemTable(this->invMenuItemsEdit, this);
    this->invMenuItemSelectionModel = new QItemSelectionModel(this->invMenuItems, this);
    this->isMenuInvSelected = false;
    ui->invMenuItems->setModel(this->invMenuItems);
    ui->invMenuItems->setSelectionModel(this->invMenuItemSelectionModel);

    connect(this->invMenuItemSelectionModel, &QItemSelectionModel::selectionChanged, this, &EditInventoryMenu::selectMenuInvItem);
    connect(ui->invMenuItemsSearch, &QLineEdit::textChanged, this, &EditInventoryMenu::filterMenuInvItems);
    connect(ui->invMenuItemsRemove, &QPushButton::clicked, this, &EditInventoryMenu::decInvItem);
    connect(ui->removeItem, &QPushButton::clicked, this, &EditInventoryMenu::removeInvItemBtn);
    connect(ui->itemAmountOverride, QOverload<int>::of(&QSpinBox::valueChanged), this, &EditInventoryMenu::requiredUnitsOverride);
}

EditInventoryMenu::~EditInventoryMenu()
{
    delete ui;
    delete invItems;
    delete invMenuItems;
}

void EditInventoryMenu::requiredUnitsOverride(int value)
{
    if (lastSpinValue != value) {
        lastSpinValue = value;

        if (!this->isMenuInvSelected) {
            QApplication::beep();
            return;
        }

        bool rm = value <= 0;
        if (!rm) {
            for (InventoryMenuItem &item : this->invMenuItemsEdit) {
                if (item.getInvUUID() == this->selectedMenuInvItem.getInvUUID()) {
                    item.setRequiredUnits(value);
                    break;
                }
            }
        }

        // Remove from the list if the quantity is now 0
        if (rm) {
            this->removeInvItem(this->selectedMenuInvItem);
        } else {
            this->invMenuItems->setItems(this->invMenuItemsEdit);
            ui->invMenuItems->setModel(NULL);
            ui->invMenuItems->setModel(this->invMenuItems);
            ui->invMenuItems->setSelectionModel(this->invMenuItemSelectionModel);
        }
    }
}


//==========
// Inventory Item Selector
//==========

void EditInventoryMenu::selectInvItem(const QItemSelection &selected, const QItemSelection deselected)
{
    InventoryItem item = this->invItems->getItem(ui->invItems->currentIndex().row());
    this->selectedInvItem = item;
    this->isInvSelected = true;
}

void EditInventoryMenu::filterInvItems(QString text)
{
    this->invItems->filter(text.toStdString());
    ui->invItems->setModel(NULL);
    ui->invItems->setModel(this->invItems);
    ui->invItems->setSelectionModel(this->invItemSelectionModel);
}

//==========
// Inventory Menu Editor
//==========

void EditInventoryMenu::selectMenuInvItem(const QItemSelection &selected, const QItemSelection deselected)
{
    InventoryMenuItem item = this->invMenuItems->getItem(ui->invMenuItems->currentIndex().row());

    lastSpinValue = item.getUnitsRequired();
    ui->itemAmountOverride->setValue(item.getUnitsRequired());
    this->selectedMenuInvItem = item;
    this->isMenuInvSelected = true;
}

void EditInventoryMenu::filterMenuInvItems(QString text)
{
    this->invMenuItems->filter(text.toStdString());
    ui->invMenuItems->setModel(NULL);
    ui->invMenuItems->setModel(this->invMenuItems);
    ui->invMenuItems->setSelectionModel(this->invMenuItemSelectionModel);
}

void EditInventoryMenu::addInvItem()
{
    if (!this->isInvSelected) {
        QApplication::beep();
        return;
    }

    bool found = false;
    for (InventoryMenuItem &item : this->invMenuItemsEdit) {
        if (item.getInvUUID() == this->selectedInvItem.getUUID()) {
            found = true;
            item.inc();

            // Inc the soinner if this is the sekected item
            if (item.getInvUUID() == this->selectedMenuInvItem.getInvUUID()) {
                ui->itemAmountOverride->setValue(item.getUnitsRequired());
            }
            break;
        }
    }

    // Add to the list if this is brand new
    if (!found) {
        InventoryMenuItem newItem = InventoryMenuItem(this->menuItem.getUUID(),
                                    this->selectedInvItem.getUUID(),
                                    this->selectedInvItem.getName(),
                                    this->selectedInvItem.getAmount(),
                                    1);
        this->invMenuItemsEdit.push_back(newItem);
    }

    this->invMenuItems->setItems(this->invMenuItemsEdit);
    ui->invMenuItems->setModel(NULL);
    ui->invMenuItems->setModel(this->invMenuItems);
    ui->invMenuItems->setSelectionModel(this->invMenuItemSelectionModel);
}

void EditInventoryMenu::removeInvItem(InventoryMenuItem item)
{
    std::list<InventoryMenuItem> tmp;
    for (InventoryMenuItem &i: this->invMenuItemsEdit) {
        if (item.getInvUUID() != i.getInvUUID()) {
            tmp.push_back(i);
        }
    }
    this->invMenuItemsEdit = tmp;

    this->invMenuItems->setItems(this->invMenuItemsEdit);
    ui->invMenuItems->setModel(NULL);
    ui->invMenuItems->setModel(this->invMenuItems);
    ui->invMenuItems->setSelectionModel(this->invMenuItemSelectionModel);
}

void EditInventoryMenu::removeInvItemBtn()
{
    this->removeInvItem(this->selectedMenuInvItem);
}

void EditInventoryMenu::decInvItem()
{
    if (!this->isMenuInvSelected) {
        QApplication::beep();
        return;
    }

    bool rm = false;
    for (InventoryMenuItem &item : this->invMenuItemsEdit) {
        if (item.getInvUUID() == this->selectedMenuInvItem.getInvUUID()) {
            rm = !item.dec();

            // Dec the soinner if this is the sekected item
            if (item.getInvUUID() == this->selectedMenuInvItem.getInvUUID()) {
                ui->itemAmountOverride->setValue(item.getUnitsRequired());
            }
            break;
        }
    }

    // Remove from the list if the quantity is now 0
    if (rm) {
        this->removeInvItem(this->selectedMenuInvItem);
    } else {
        this->invMenuItems->setItems(this->invMenuItemsEdit);
        ui->invMenuItems->setModel(NULL);
        ui->invMenuItems->setModel(this->invMenuItems);
        ui->invMenuItems->setSelectionModel(this->invMenuItemSelectionModel);
    }
}

void EditInventoryMenu::complete()
{
    if (this->waitingForSave) {
        QApplication::beep();
    } else {
        if (this->config->updateInvMenuItems(this->menuItem, this->invMenuItemsEdit)) {
            emit this->onComplete();
            emit this->accept();
        } else {

            QApplication::beep();
            std::cerr << "Failed to add new item"
                      << std::endl;

            QMessageBox msgBox;
            msgBox.setText("There was an error, please try again.");
            msgBox.exec();
        }

        this->waitingForSave = false;
    }
}
