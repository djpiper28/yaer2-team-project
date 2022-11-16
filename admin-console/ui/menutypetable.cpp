#include <sstream>
#include <iterator>
#include <vector>
#include <algorithm>
#include "menutypetable.h"

MenuTypeTable::MenuTypeTable(std::list<MenuType> menu, Configuration *config, bool isCombo, QObject *parent)
    : QAbstractTableModel(parent)
{
    this->menu = FilteredList(menu);
    this->config = config;
    this->isCombo = isCombo;
}

int MenuTypeTable::rowCount(const QModelIndex &parent) const
{
    return (int) this->menu.size();
}

int MenuTypeTable::columnCount(const QModelIndex &parent) const
{
    return this->isCombo ? 1 : MENU_TYPE_TABLE_COLUMNS;
}

QVariant MenuTypeTable::headerData(int section, Qt::Orientation orientation, int role) const
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
        return QVariant("Image");
    default:
        return QVariant();
    }
}

QVariant MenuTypeTable::data(const QModelIndex &index, int role) const
{
    if (!index.isValid() || role != Qt::DisplayRole) {
        return QVariant();
    }

    if (index.row() >= (int) this->menu.size() || index.column() >= MENU_TYPE_TABLE_COLUMNS) {
        return QVariant();
    }

    MenuType item = this->getItem(index.row());
    switch (index.column()) {
    case 0:
        return QVariant(QString::fromStdString(item.getName()));
    case 1:
        return QVariant(QString::fromStdString(item.getImage()));
    default:
        return QVariant();
    }
}

MenuType MenuTypeTable::getItem(int i) const
{
    int count = 0;
    for (MenuType item: this->menu.getFiltered()) {
        if (count == i) {
            return item;
        }

        count++;
    }

    return MenuType();
}

Qt::ItemFlags MenuTypeTable::flags(const QModelIndex &index) const
{
    if(!index.isValid()) return Qt::ItemIsEnabled;
    return QAbstractTableModel::flags(index) | Qt::ItemIsSelectable | Qt::ItemIsEnabled;
}

void MenuTypeTable::filter(std::string filter)
{
    this->menu.filter(filter);
    this->updateTable();
}

void MenuTypeTable::setMenuTypes(std::list<MenuType> menu)
{
    this->menu.setBase(menu);
    this->updateTable();
}

void MenuTypeTable::updateTable()
{
    QModelIndex topLeft = this->index(0, 0);
    QModelIndex bottomRight = this->index(this->menu.size() - 1, MENU_TYPE_TABLE_COLUMNS - 1);
    emit this->dataChanged(topLeft, bottomRight);
}
