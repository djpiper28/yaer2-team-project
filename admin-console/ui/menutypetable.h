#pragma once
#include <QAbstractTableModel>
#include <QVariant>
#include "../model/configuration.h"
#include "../model/menutype.h"
#include "filteredlist.h"

// name, price, active, in stock, desc
#define SPACES "   "
#define MENU_TYPE_TABLE_COLUMNS 2

class MenuTypeTable : public QAbstractTableModel
{
    Q_OBJECT
public:
    explicit MenuTypeTable(std::list<MenuType> menu, Configuration *config, bool isCombo, QObject *parent = nullptr);
    int rowCount(const QModelIndex &parent = QModelIndex()) const override;
    int columnCount(const QModelIndex &parent = QModelIndex()) const override;
    QVariant headerData(int section, Qt::Orientation orientation, int role) const override;
    QVariant data(const QModelIndex &index, int role = Qt::DisplayRole) const override;
    Qt::ItemFlags flags(const QModelIndex &index) const override;
    MenuType getItem(int i) const;
    void setMenuTypes(std::list<MenuType> menu);
    void filter(std::string filter);
private:
    bool isCombo;
    void updateTable();
    FilteredList<MenuType> menu;
    Configuration *config;
};
