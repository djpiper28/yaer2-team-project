#pragma once
#include <string>

#define USER_CUSTOMER 0
#define USER_KITCHEN 1
#define USER_WAITER 2

class User
{
public:
    User(); // Deferred init
    User(std::string uuid, std::string fname, std::string sname,
         std::string email, std::string phoneno, int usetype);
    User(std::string fname, std::string sname, std::string email,
         std::string phoneno, int usetype);
    std::string getUUID();
    std::string getFName();
    std::string getSName();
    std::string getEmail();
    std::string getPhoneNo();
    int getUseType();
    bool matches(std::string query);
private:
    void init(std::string uuid, std::string fname, std::string sname,
              std::string email, std::string phoneno, int usetype);
    std::string uuid;
    std::string fname;
    std::string sname;
    std::string email;
    std::string phoneno;
    int usetype;
};
