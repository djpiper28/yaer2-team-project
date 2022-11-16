#pragma once
#include <QAbstractTableModel>
#include <QVariant>
#include "../model/inventoryitem.h"
#include "filteredlist.h"

// name, amount
#define INV_ITEM_TABLE_COLUMNS 2

class InventoryItemTable : public QAbstractTableModel
{
    Q_OBJECT
public:
    explicit InventoryItemTable(std::list<InventoryItem> items, QObject *parent = nullptr);
    int rowCount(const QModelIndex &parent = QModelIndex()) const override;
    int columnCount(const QModelIndex &parent = QModelIndex()) const override;
    QVariant headerData(int section, Qt::Orientation orientation, int role) const override;
    QVariant data(const QModelIndex &index, int role = Qt::DisplayRole) const override;
    Qt::ItemFlags flags(const QModelIndex &index) const override;
    InventoryItem getItem(int i) const;
    void setItems(std::list<InventoryItem> items);
    void filter(std::string filter);
private:
    void updateTable();
    FilteredList<InventoryItem> items;
};
