#include "user.h"
#include <QUuid>

void User::init(std::string uuid, std::string fname, std::string sname,
                std::string email, std::string phoneno, int usetype)
{
    this->uuid = uuid;
    this->fname = fname;
    this->sname = sname;
    this->email = email;
    this->phoneno = phoneno;
    this->usetype = usetype;
}

User::User()
{

}

User::User(std::string uuid, std::string fname, std::string sname,
           std::string email, std::string phoneno, int usetype)
{
    this->init(uuid, fname, sname, email, phoneno, usetype);
}

User::User(std::string fname, std::string sname, std::string email,
           std::string phoneno, int usetype)
{
    QUuid uuid = QUuid::createUuid();
    std::string uuid_str = uuid.toString().toStdString().substr(1);
    uuid_str.pop_back();
    this->init(uuid_str, fname, sname, email, phoneno, usetype);
}

bool User::matches(std::string query)
{
    size_t pos = 0;
    const std::string space_delimiter = " ";
    std::string filter = query + " "; // Funny hack

    while ((pos = filter.find(space_delimiter)) != std::string::npos) {
        std::string segment = filter.substr(0, pos);

        if (segment != " ") {
            bool found = false;

            if (this->uuid.find(segment) != std::string::npos) found = true;
            if (this->fname.find(segment) != std::string::npos) found = true;
            if (this->sname.find(segment) != std::string::npos) found = true;
            if (this->phoneno.find(segment) != std::string::npos) found = true;
            switch (this->usetype) {
            case USER_CUSTOMER:
                found |= std::string("Customer").find(segment) != std::string::npos;
                break;
            case USER_KITCHEN:
                found |= std::string("Kitchen").find(segment) != std::string::npos;
                found |= std::string("Staff").find(segment) != std::string::npos;
                break;
            case USER_WAITER:
                found |= std::string("Waiter").find(segment) != std::string::npos;
                found |= std::string("Staff").find(segment) != std::string::npos;
                break;
            }

            if (found) {
                return true;
            }
        }

        filter.erase(0, pos + space_delimiter.length());
    }

    return false;
}

std::string User::getUUID()
{
    return this->uuid;
}

std::string User::getFName()
{
    return this->fname;
}

std::string User::getSName()
{
    return this->sname;
}

std::string User::getEmail()
{
    return this->email;
}

std::string User::getPhoneNo()
{
    return this->phoneno;
}

int User::getUseType()
{
    return this->usetype;
}

