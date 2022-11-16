#pragma once
#include <string>
#include <pqxx/pqxx>
class Configuration;

class MenuItem
{
public:
    // Constructor to allow for init deferation
    MenuItem();
    // Constructor that generates a UUID
    MenuItem(std::string name, std::string description, std::string image_uri,
             std::string typeuuid, float price, bool active, Configuration *config,
             int prepTime);
    // Constructor that takes a UUID
    MenuItem(std::string name, std::string description, std::string image_uri,
             std::string uuid, std::string typeuuid, float price, bool active,
             int prepTime);
    MenuItem(std::string name, std::string description, std::string image_uri,
             std::string uuid, std::string typeuuid, float price, bool active,
             Configuration *config, int prepTime);
    std::string getName();
    std::string getDescription();
    std::string getImageURI();
    std::string getUUID();
    std::string getTypeUUID();
    float getPrice();
    bool isActive();
    bool inStock();
    bool matches(std::string query);
    int getPrepTime();
private:
    void init(std::string name, std::string description, std::string image_uri,
              std::string uuid, std::string typeuuid, float price, bool active,
              Configuration *config, int prepTime);
    std::string name, description, image_uri, uuid;
    std::string typeuuid;
    float price;
    bool instock;
    bool active;
    int prepTime;
};
