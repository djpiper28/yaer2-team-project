#pragma once
#include <QAbstractTableModel>
#include <QVariant>
#include <string>
#include "../model/inventorymenuitem.h"
#include "filteredlist.h"

// name, amount
#define INV_MENU_ITEM_TABLE_COLUMNS 3

class InventoryMenuItemTable : public QAbstractTableModel
{
    Q_OBJECT
public:
    explicit InventoryMenuItemTable(std::list<InventoryMenuItem> items, QObject *parent = nullptr);
    int rowCount(const QModelIndex &parent = QModelIndex()) const override;
    int columnCount(const QModelIndex &parent = QModelIndex()) const override;
    QVariant headerData(int section, Qt::Orientation orientation, int role) const override;
    QVariant data(const QModelIndex &index, int role = Qt::DisplayRole) const override;
    Qt::ItemFlags flags(const QModelIndex &index) const override;
    InventoryMenuItem getItem(int i) const;
    void setItems(std::list<InventoryMenuItem> items);
    void filter(std::string filter);
private:
    void updateTable();
    FilteredList<InventoryMenuItem> items;
};
