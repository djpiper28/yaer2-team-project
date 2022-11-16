#pragma once
#include <string>

class MenuType
{
public:
    MenuType(); // Deferred init
    MenuType(std::string name, std::string image, std::string desc);
    MenuType(std::string uuid, std::string name, std::string image, std::string desc);
    std::string getUUID();
    std::string getName();
    std::string getImage();
    std::string getDesc();
    bool matches(std::string query);
private:
    void init(std::string uuid, std::string name, std::string image, std::string desc);
    std::string uuid;
    std::string name;
    std::string image;
    std::string desc;
};
