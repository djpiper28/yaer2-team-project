#include <sstream>
#include <iterator>
#include <vector>
#include <algorithm>
#include "menuitemtable.h"

MenuItemTable::MenuItemTable(std::list<MenuItem> menu, Configuration *config, QObject *parent)
    : QAbstractTableModel(parent)
{
    this->menu = FilteredList(menu);
    this->config = config;
}

int MenuItemTable::rowCount(const QModelIndex &parent) const
{
    return (int) this->menu.size();
}

int MenuItemTable::columnCount(const QModelIndex &parent) const
{
    return MENU_ITEM_TABLE_COLUMNS;
}

QVariant MenuItemTable::headerData(int section, Qt::Orientation orientation, int role) const
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
        return QVariant("Price");
    case 2:
        return QVariant("Active");
    case 3:
        return QVariant("In Stock");
    case 4:
        return QVariant("Description");
    default:
        return QVariant();
    }
}

QVariant MenuItemTable::data(const QModelIndex &index, int role) const
{
    if (!index.isValid() || role != Qt::DisplayRole) {
        return QVariant();
    }

    if (index.row() >= (int) this->menu.size() || index.column() >= MENU_ITEM_TABLE_COLUMNS) {
        return QVariant();
    }

    MenuItem item = this->getItem(index.row());
    switch (index.column()) {
    case 0:
        return QVariant(QString::fromStdString(item.getName()));
    case 1:
        return QVariant(item.getPrice());
    case 2:
        return QVariant(QString::fromStdString(SPACES + std::string(item.isActive() ? "✓" : "X")));
    case 3:
        return QVariant(QString::fromStdString(item.inStock() ? "✓" : "X"));
    case 4:
        return QVariant(QString::fromStdString(item.getDescription()));
    default:
        return QVariant();
    }
}

MenuItem MenuItemTable::getItem(int i) const
{
    int count = 0;
    for (MenuItem item: this->menu.getFiltered()) {
        if (count == i) {
            return item;
        }

        count++;
    }

    return MenuItem();
}

Qt::ItemFlags MenuItemTable::flags(const QModelIndex &index) const
{
    if(!index.isValid()) return Qt::ItemIsEnabled;
    return QAbstractTableModel::flags(index) | Qt::ItemIsSelectable | Qt::ItemIsEnabled;
}

void MenuItemTable::filter(std::string filter)
{
    this->menu.filter(filter);
    this->updateTable();
}

void MenuItemTable::setMenu(std::list<MenuItem> menu)
{
    this->menu.setBase(menu);
    this->updateTable();
}

void MenuItemTable::updateTable()
{
    QModelIndex topLeft = this->index(0, 0);
    QModelIndex bottomRight = this->index(this->menu.size() - 1, MENU_ITEM_TABLE_COLUMNS - 1);
    emit this->dataChanged(topLeft, bottomRight);
}
