#include <sstream>
#include <iterator>
#include <vector>
#include <algorithm>
#include "usertable.h"

UserTable::UserTable(std::list<User> users, QObject *parent)
    : QAbstractTableModel(parent)
{
    this->users = FilteredList(users);
}

int UserTable::rowCount(const QModelIndex &parent) const
{
    return (int) this->users.size();
}

int UserTable::columnCount(const QModelIndex &parent) const
{
    return USER_TABLE_COLUMNS;
}

QVariant UserTable::headerData(int section, Qt::Orientation orientation, int role) const
{
    if (role != Qt::DisplayRole) {
        return QVariant();
    }

    if (orientation == Qt::Orientation::Vertical) {
        return QVariant(section + 1);
    }


    // type, email, fname, sname, phone
    switch (section) {
    case 0:
        return QVariant("Account Type");
    case 1:
        return QVariant("Email");
    case 2:
        return QVariant("Firstname");
    case 3:
        return QVariant("Surname");
    case 4:
        return QVariant("Phone Number");
    default:
        return QVariant();
    }
}

QVariant UserTable::data(const QModelIndex &index, int role) const
{
    if (!index.isValid() || role != Qt::DisplayRole) {
        return QVariant();
    }

    if (index.row() >= (int) this->users.size() || index.column() >= USER_TABLE_COLUMNS) {
        return QVariant();
    }

    User user = this->getUser(index.row());
    switch (index.column()) {
    case 0:
        switch(user.getUseType()) {
        case USER_CUSTOMER:
            return QVariant("Customer");
        case USER_KITCHEN:
            return QVariant("Kitchen Staff");
        case USER_WAITER:
            return QVariant("Waiter Staff");
        default:
            return QVariant();
        }
    case 1:
        return QVariant(QString::fromStdString(user.getEmail()));
    case 2:
        return QVariant(QString::fromStdString(user.getFName()));
    case 3:
        return QVariant(QString::fromStdString(user.getSName()));
    case 4:
        return QVariant(QString::fromStdString(user.getPhoneNo()));
    default:
        return QVariant();
    }
}

User UserTable::getUser(int i) const
{
    int count = 0;
    for (User user: this->users.getFiltered()) {
        if (count == i) {
            return user;
        }

        count++;
    }

    return User();
}

Qt::ItemFlags UserTable::flags(const QModelIndex &index) const
{
    if(!index.isValid()) return Qt::ItemIsEnabled;
    return QAbstractTableModel::flags(index) | Qt::ItemIsSelectable | Qt::ItemIsEnabled;
}

void UserTable::filter(std::string filter)
{
    this->users.filter(filter);
    this->updateTable();
}

void UserTable::setUsers(std::list<User> users)
{
    this->users.setBase(users);
    this->updateTable();
}

void UserTable::updateTable()
{
    QModelIndex topLeft = this->index(0, 0);
    QModelIndex bottomRight = this->index(this->users.size() - 1, USER_TABLE_COLUMNS - 1);
    emit this->dataChanged(topLeft, bottomRight);
}
