#pragma once
#include <string>

class InventoryMenuItem
{
public:
    InventoryMenuItem();
    InventoryMenuItem(std::string menuid, std::string invid, std::string name,
                      int amountInStock, int requiredUnits);
    std::string getMenuUUID();
    std::string getInvUUID();
    std::string getName();
    int getAmountInStock();
    int getUnitsRequired();
    bool matches(std::string query);
    void inc();
    bool dec(); // True if there are more than zero after the operation
    void setRequiredUnits(int i);
private:
    std::string menuid;
    std::string invid;
    std::string name;
    int amountInStock;
    int requiredUnits;
};
