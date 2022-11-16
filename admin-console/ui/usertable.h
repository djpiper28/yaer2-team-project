#pragma once
#include <QAbstractTableModel>
#include <QVariant>
#include "../model/user.h"
#include "filteredlist.h"

// type, email, fname, sname, phone
#define USER_TABLE_COLUMNS 5

class UserTable : public QAbstractTableModel
{
    Q_OBJECT
public:
    explicit UserTable(std::list<User> users, QObject *parent = nullptr);
    int rowCount(const QModelIndex &parent = QModelIndex()) const override;
    int columnCount(const QModelIndex &parent = QModelIndex()) const override;
    QVariant headerData(int section, Qt::Orientation orientation, int role) const override;
    QVariant data(const QModelIndex &index, int role = Qt::DisplayRole) const override;
    Qt::ItemFlags flags(const QModelIndex &index) const override;
    User getUser(int i) const;
    void setUsers(std::list<User> users);
    void filter(std::string filter);
private:
    void updateTable();
    FilteredList<User> users;
};
