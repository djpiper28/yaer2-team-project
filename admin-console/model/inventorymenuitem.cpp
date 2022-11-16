#include "inventorymenuitem.h"

InventoryMenuItem::InventoryMenuItem()
{

}

InventoryMenuItem::InventoryMenuItem(std::string menuid, std::string invid, std::string name,
                                     int amountInStock, int requiredUnits)
{
    this->menuid = menuid;
    this->invid = invid;
    this->name = name;
    this->amountInStock = amountInStock;
    this->requiredUnits = requiredUnits;
}

void InventoryMenuItem::inc()
{
    this->requiredUnits++;
}

void InventoryMenuItem::setRequiredUnits(int i)
{
    this->requiredUnits = i;
}

bool InventoryMenuItem::dec()
{
    this->requiredUnits--;
    if (this->requiredUnits <= 0) {
        return false;
    } else {
        return true;
    }
}

std::string InventoryMenuItem::getMenuUUID()
{
    return this->menuid;
}

std::string InventoryMenuItem::getInvUUID()
{
    return this->invid;
}

std::string InventoryMenuItem::getName()
{
    return this->name;
}

int InventoryMenuItem::getAmountInStock()
{
    return this->amountInStock;
}

int InventoryMenuItem::getUnitsRequired()
{
    return this->requiredUnits;
}

bool InventoryMenuItem::matches(std::string query)
{
    size_t pos = 0;
    const std::string space_delimiter = " ";
    std::string filter = query + " "; // Funny hack

    while ((pos = filter.find(space_delimiter)) != std::string::npos) {
        std::string segment = filter.substr(0, pos);

        if (segment != " ") {
            bool found = false;

            if (this->menuid.find(segment) != std::string::npos) found = true;
            if (this->invid.find(segment) != std::string::npos) found = true;
            if (this->name.find(segment) != std::string::npos) found = true;
            if (std::abs(this->amountInStock - atof(segment.c_str())) < 0.001) found = true;
            if (std::abs(this->requiredUnits - atof(segment.c_str())) < 0.001) found = true;

            if (found) {
                return true;
            }
        }

        filter.erase(0, pos + space_delimiter.length());
    }

    return false;
}
