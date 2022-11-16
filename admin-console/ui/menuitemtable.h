#pragma once
#include <QAbstractTableModel>
#include <QVariant>
#include "../model/configuration.h"
#include "../model/menuitem.h"
#include "filteredlist.h"

// name, price, active, in stock, desc
#define SPACES "   "
#define MENU_ITEM_TABLE_COLUMNS 5

class MenuItemTable : public QAbstractTableModel
{
    Q_OBJECT
public:
    explicit MenuItemTable(std::list<MenuItem> menu, Configuration *config, QObject *parent = nullptr);
    int rowCount(const QModelIndex &parent = QModelIndex()) const override;
    int columnCount(const QModelIndex &parent = QModelIndex()) const override;
    QVariant headerData(int section, Qt::Orientation orientation, int role) const override;
    QVariant data(const QModelIndex &index, int role = Qt::DisplayRole) const override;
    Qt::ItemFlags flags(const QModelIndex &index) const override;
    MenuItem getItem(int i) const;
    void setMenu(std::list<MenuItem> menu);
    void filter(std::string filter);
private:
    void updateTable();
    FilteredList<MenuItem> menu;
    Configuration *config;
};
