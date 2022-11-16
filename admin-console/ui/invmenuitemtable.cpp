#include <sstream>
#include <iterator>
#include <vector>
#include <algorithm>
#include "invmenuitemtable.h"

InventoryMenuItemTable::InventoryMenuItemTable(std::list<InventoryMenuItem> items, QObject *parent)
    : QAbstractTableModel(parent)
{
    this->items = FilteredList(items);
}

int InventoryMenuItemTable::rowCount(const QModelIndex &parent) const
{
    return (int) this->items.size();
}

int InventoryMenuItemTable::columnCount(const QModelIndex &parent) const
{
    return INV_MENU_ITEM_TABLE_COLUMNS;
}

QVariant InventoryMenuItemTable::headerData(int section, Qt::Orientation orientation, int role) const
{
    if (role != Qt::DisplayRole) {
        return QVariant();
    }

    if (orientation == Qt::Orientation::Vertical) {
        return QVariant(section + 1);
    }

    switch (section) {
    case 0:
        return QVariant("Name");
    case 1:
        return QVariant("Amount Per Item");
    case 2:
        return QVariant("Amount in Stock");
    default:
        return QVariant();
    }
}

QVariant InventoryMenuItemTable::data(const QModelIndex &index, int role) const
{
    if (!index.isValid() || role != Qt::DisplayRole) {
        return QVariant();
    }

    if (index.row() >= (int) this->items.size() || index.column() >= INV_MENU_ITEM_TABLE_COLUMNS) {
        return QVariant();
    }

    InventoryMenuItem item = this->getItem(index.row());
    switch (index.column()) {
    case 0:
        return QVariant(QString::fromStdString(item.getName()));
    case 1:
        return QVariant(item.getUnitsRequired());
    case 2:
        return QVariant(item.getAmountInStock());
    default:
        return QVariant();
    }
}

InventoryMenuItem InventoryMenuItemTable::getItem(int i) const
{
    int count = 0;
    for (InventoryMenuItem item: this->items.getFiltered()) {
        if (count == i) {
            return item;
        }

        count++;
    }

    return InventoryMenuItem();
}

Qt::ItemFlags InventoryMenuItemTable::flags(const QModelIndex &index) const
{
    if(!index.isValid()) return Qt::ItemIsEnabled;
    return QAbstractTableModel::flags(index) | Qt::ItemIsSelectable | Qt::ItemIsEnabled;
}

void InventoryMenuItemTable::filter(std::string filter)
{
    this->items.filter(filter);
    this->updateTable();
}

void InventoryMenuItemTable::setItems(std::list<InventoryMenuItem> menu)
{
    this->items.setBase(menu);
    this->updateTable();
}

void InventoryMenuItemTable::updateTable()
{
    QModelIndex topLeft = this->index(0, 0);
    QModelIndex bottomRight = this->index(this->items.size() - 1, INV_MENU_ITEM_TABLE_COLUMNS - 1);
    emit this->dataChanged(topLeft, bottomRight);
}
