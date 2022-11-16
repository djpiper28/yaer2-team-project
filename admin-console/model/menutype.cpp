#include <QUuid>
#include "menutype.h"

MenuType::MenuType()
{

}

MenuType::MenuType(std::string name, std::string image, std::string desc)
{
    QUuid uuid = QUuid::createUuid();
    std::string uuid_str = uuid.toString().toStdString().substr(1);
    uuid_str.pop_back();
    this->init(uuid_str, name, image, desc);
}

MenuType::MenuType(std::string uuid, std::string name, std::string image, std::string desc)
{
    this->init(uuid, name, image, desc);
}

void MenuType::init(std::string uuid, std::string name, std::string image, std::string desc)
{
    this->uuid = uuid;
    this->name = name;
    this->image = image;
    this->desc = desc;
}

std::string MenuType::getUUID()
{
    return this->uuid;
}

std::string MenuType::getName()
{
    return this->name;
}

std::string MenuType::getImage()
{
    return this->image;
}

std::string MenuType::getDesc()
{
    return this->desc;
}

bool MenuType::matches(std::string query)
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
            if (this->image.find(segment) != std::string::npos) found = true;
            if (this->desc.find(segment) != std::string::npos) found = true;

            if (found) {
                return true;
            }
        }

        filter.erase(0, pos + space_delimiter.length());
    }

    return false;
}
