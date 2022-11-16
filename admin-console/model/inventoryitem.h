#pragma once
#include <string>
#include <list>
#include <string>

class InventoryItem
{
public:
    InventoryItem();
    InventoryItem(std::string name, int amount);
    InventoryItem(std::string uuid, std::string name, int amount);
    std::string getUUID();
    std::string getName();
    int getAmount();
    bool matches(std::string query);
private:
    std::string uuid;
    std::string name;
    int amount;
    void init(std::string uuid, std::string name, int amount);
};
