#include <QUuid>
#include "menuitem.h"
#include "configuration.h"

void MenuItem::init(std::string name, std::string description, std::string image_uri,
                    std::string uuid, std::string typeuuid, float price, bool active,
                    Configuration *config, int prepTime)
{
    this->name = name;
    this->description = description;
    this->image_uri = image_uri;
    this->uuid = uuid;
    this->typeuuid = typeuuid;
    this->price = price;
    this->active = active;
    this->prepTime = prepTime;
    if (config == nullptr) {
        this->instock = false;
    } else {
        this->instock = config->isInStock(*this);
    }
}

MenuItem::MenuItem()
{

}

MenuItem::MenuItem(std::string name, std::string description, std::string image_uri,
                   std::string uuid, std::string typeuuid, float price, bool active,
                   Configuration *config, int prepTime)
{
    this->init(name, description, image_uri, uuid, typeuuid, price, active, config, prepTime);
}

MenuItem::MenuItem(std::string name, std::string description, std::string image_uri,
                   std::string uuid, std::string typeuuid, float price, bool active,
                   int prepTime)
{
    this->init(name, description, image_uri, uuid, typeuuid, price, active, nullptr, prepTime);
}

MenuItem::MenuItem(std::string name, std::string description, std::string image_uri,
                   std::string typeuuid, float price, bool active,
                   Configuration *config, int prepTime)
{
    QUuid uuid = QUuid::createUuid();
    std::string uuid_str = uuid.toString().toStdString().substr(1);
    uuid_str.pop_back();
    this->init(name, description, image_uri, uuid_str, typeuuid, price, active, config, prepTime);
}

std::string MenuItem::getName()
{
    return std::string(this->name);
}

std::string MenuItem::getDescription()
{
    return std::string(this->description);
}

std::string MenuItem::getImageURI()
{
    return std::string(image_uri);
}

std::string MenuItem::getUUID()
{
    return std::string(this->uuid);
}

float MenuItem::getPrice()
{
    return this->price;
}

bool MenuItem::isActive()
{
    return this->active;
}

bool MenuItem::inStock()
{
    return this->instock;
}

std::string MenuItem::getTypeUUID()
{
    return this->typeuuid;
}

int MenuItem::getPrepTime()
{
    return this->prepTime;
}

bool MenuItem::matches(std::string query)
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
            if (this->description.find(segment) != std::string::npos) found = true;
            if (this->image_uri.find(segment) != std::string::npos) found = true;
            if (std::abs(this->price - atof(segment.c_str())) < 0.001) found = true;
            if (this->typeuuid.find(segment) != std::string::npos) found = true;
            if (std::to_string(this->prepTime) == segment) found = true;

            if (found) {
                return true;
            }
        }

        filter.erase(0, pos + space_delimiter.length());
    }

    return false;
}
