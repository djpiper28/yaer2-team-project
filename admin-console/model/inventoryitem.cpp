#include <QUuid>
#include <pqxx/pqxx>
#include "inventoryitem.h"

InventoryItem::InventoryItem()
{

}

InventoryItem::InventoryItem(std::string name, int amount)
{
    QUuid uuid = QUuid::createUuid();
    std::string uuid_str = uuid.toString().toStdString().substr(1);
    uuid_str.pop_back();
    this->init(uuid_str, name, amount);
}

InventoryItem::InventoryItem(std::string uuid, std::string name, int amount)
{
    this->init(uuid, name, amount);
}

void InventoryItem::init(std::string uuid, std::string name, int amount)
{
    this->uuid = uuid;
    this->name = name;
    this->amount = amount;
}

std::string InventoryItem::getUUID()
{
    return this->uuid;
}

std::string InventoryItem::getName()
{
    return this->name;
}

int InventoryItem::getAmount()
{
    return this->amount;
}

bool InventoryItem::matches(std::string query)
{
    size_t pos = 0;
    const std::string space_delimiter = " ";
    std::string filter = query + " "; // Funny hack

    while ((pos = filter.find(space_delimiter)) != std::string::npos) {
        std::string segment = filter.substr(0, pos);

        if (segment != " ") {
            bool found = false;

            if (this->uuid.find(segment) != std::string::npos) found = true;
            if (this->name.find(segment) != std::string::npos) found = true;
            if (std::abs(this->amount - atof(segment.c_str())) < 0.001) found = true;

            if (found) {
                return true;
            }
        }

        filter.erase(0, pos + space_delimiter.length());
    }

    return false;
}
