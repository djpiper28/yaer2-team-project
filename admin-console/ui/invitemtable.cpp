#include <sstream>
#include <iterator>
#include <vector>
#include <algorithm>
#include "invitemtable.h"

InventoryItemTable::InventoryItemTable(std::list<InventoryItem> items, QObject *parent)
    : QAbstractTableModel(parent)
{
    this->items = FilteredList(items);
}

int InventoryItemTable::rowCount(const QModelIndex &parent) const
{
    return (int) this->items.size();
}

int InventoryItemTable::columnCount(const QModelIndex &parent) const
{
    return INV_ITEM_TABLE_COLUMNS;
}

QVariant InventoryItemTable::headerData(int section, Qt::Orientation orientation, int role) const
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
        return QVariant("Amount in Stock");
    default:
        return QVariant();
    }
}

QVariant InventoryItemTable::data(const QModelIndex &index, int role) const
{
    if (!index.isValid() || role != Qt::DisplayRole) {
        return QVariant();
    }

    if (index.row() >= (int) this->items.size() || index.column() >= INV_ITEM_TABLE_COLUMNS) {
        return QVariant();
    }

    InventoryItem item = this->getItem(index.row());
    switch (index.column()) {
    case 0:
        return QVariant(QString::fromStdString(item.getName()));
    case 1:
        return QVariant(item.getAmount());
    default:
        return QVariant();
    }
}

InventoryItem InventoryItemTable::getItem(int i) const
{
    int count = 0;
    for (InventoryItem item: this->items.getFiltered()) {
        if (count == i) {
            return item;
        }

        count++;
    }

    return InventoryItem();
}

Qt::ItemFlags InventoryItemTable::flags(const QModelIndex &index) const
{
    if(!index.isValid()) return Qt::ItemIsEnabled;
    return QAbstractTableModel::flags(index) | Qt::ItemIsSelectable | Qt::ItemIsEnabled;
}

void InventoryItemTable::filter(std::string filter)
{
    this->items.filter(filter);
    this->updateTable();
}

void InventoryItemTable::setItems(std::list<InventoryItem> menu)
{
    this->items.setBase(menu);
    this->updateTable();
}

void InventoryItemTable::updateTable()
{
    QModelIndex topLeft = this->index(0, 0);
    QModelIndex bottomRight = this->index(this->items.size() - 1, INV_ITEM_TABLE_COLUMNS - 1);
    emit this->dataChanged(topLeft, bottomRight);
}
