#include <iostream>
#include <exception>
#include <QJsonObject>
#include <QJsonDocument>
#include <QMessageBox>
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <stdlib.h>
#include "configuration.h"
#include "../utils.h"
#include "../security.h"

Configuration::Configuration()
{
    this->conn = nullptr;

    // Read config
    FILE *f = fopen(CONFIGURATION_FILE, "r");
    bool error = f == NULL;

    // Give default values on error
    if (!error) {
        size_t ptr = 0, length = BUFFER_INC;
        char *buffer = (char *) malloc(sizeof * buffer * length);
        int c;

        while ((c = getc(f)) != EOF) {
            if (ptr + 1 >= length) {
                length += BUFFER_INC;
                buffer = (char *) realloc(buffer, length);

                if (buffer == NULL) {
                    error = true;
                    break;
                }
            }

            buffer[ptr++] = (char) c;
        }
        buffer[ptr] = 0;

        fclose(f);

        if (!error) {
            std::string json = std::string(buffer);
            free(buffer);

            QString qjson = QString::fromStdString(json);
            QJsonDocument confDoc = QJsonDocument::fromJson(qjson.toUtf8());
            error = !confDoc.isObject();

            if (error) {
                std::cerr << "Config file is corrupt"
                          << std::endl;
            } else {
                QJsonObject obj = confDoc.object();
                bool contentsValid = obj.contains(CONFIG_DB_NAME)
                                     && obj.contains(CONFIG_DB_USERNAME)
                                     && obj.contains(CONFIG_DB_PASSWORD)
                                     && obj.contains(CONFIG_DB_PORT)
                                     && obj.contains(CONFIG_DB_HOST)
                                     && obj.contains(CONFIG_CDN_ROOT);
                if (contentsValid) {
                    contentsValid = obj[CONFIG_DB_NAME].isString()
                                    && obj[CONFIG_DB_USERNAME].isString()
                                    && obj[CONFIG_DB_PASSWORD].isString()
                                    && obj[CONFIG_DB_HOST].isString()
                                    && obj[CONFIG_CDN_ROOT].isString()
                                    && obj[CONFIG_DB_PORT].isDouble();
                    error = !contentsValid;

                    if (!error) {
                        this->db_name = obj[CONFIG_DB_NAME].toString().toStdString();
                        this->db_username = obj[CONFIG_DB_USERNAME].toString().toStdString();
                        this->db_password= obj[CONFIG_DB_PASSWORD].toString().toStdString();
                        this->db_host_address = obj[CONFIG_DB_HOST].toString().toStdString();
                        this->cdn_root = obj[CONFIG_CDN_ROOT].toString().toStdString();
                        this->db_port = round(obj[CONFIG_DB_PORT].toDouble());
                    }
                } else {
                    std::cerr << "Config file is corrupt as it is missing some values"
                              << std::endl;
                    error = true;
                }
            }
        }
    }

    if(error) {
        std::cerr << "Error loading config, maybe it is not there"
                  << std::endl;

        this->db_name = "teamdev";
        this->db_username = "dev";
        this->db_password = "REDACTED";
        this->db_port = 6445;
        this->db_host_address = "www.djpiper28.co.uk";
        this->cdn_root = "../../cdn/";
        this->save();
    }
}

Configuration::~Configuration()
{
    if (this->conn != nullptr) {
        delete this->conn;
    }
}

bool Configuration::save()
{
    QJsonObject save_obj;
    save_obj.insert(CONFIG_DB_NAME, QJsonValue(QString::fromStdString(this->db_name)));
    save_obj.insert(CONFIG_DB_USERNAME, QJsonValue(QString::fromStdString(this->db_username)));
    save_obj.insert(CONFIG_DB_PASSWORD, QJsonValue(QString::fromStdString(this->db_password)));
    save_obj.insert(CONFIG_DB_PORT, QJsonValue(this->db_port));
    save_obj.insert(CONFIG_DB_HOST, QJsonValue(QString::fromStdString(this->db_host_address)));
    save_obj.insert(CONFIG_CDN_ROOT, QJsonValue(QString::fromStdString(this->cdn_root)));

    QJsonDocument doc = QJsonDocument(save_obj);
    QString strJson(doc.toJson(QJsonDocument::Indented));

    FILE *f = fopen(CONFIGURATION_FILE, "w");
    if (f == NULL) {
        return false;
    }

    fprintf(f, "%s\n", strJson.toStdString().c_str());
    fclose(f);

    try {
        this->getConnection()->close(); // Close the connection
    } catch (std::exception e) {
        std::cerr << e.what() << std::endl;
        return false;
    }

    return true;
}

const size_t VALID_INPUT_LENGTH = strlen(VALID_INPUT);

bool Configuration::isInputValid(std::string input)
{
    bool valid = false;
    const char *in = input.c_str();
    for (size_t i = 0; i < input.size() && !valid; i++) {
        for (size_t j = 0; j < VALID_INPUT_LENGTH && !valid; j++) {
            valid = in[i] == VALID_INPUT[j];
        }
    }

    return valid;
}

std::string Configuration::toCdnPath(std::string filepath)
{
    // Get the real path of the root
    std::string cdnPath = this->getCdnRoot();
    char *cdnRootPath = (char *) cdnPath.c_str();
    char actualpath [PATH_MAX + 1]; // NULL terminator
    realpath(cdnRootPath, actualpath);
    const std::string cdnRealPath = std::string(actualpath);

    // Get the real path of the image
    const std::string filePath = filepath;
    char *filePath_c = (char *) filePath.c_str();
    realpath(filePath_c, actualpath);
    const std::string realFilePath = std::string(actualpath);

    std::string newImagePath = filePath; // This will be changed to the path of the copy if there is one
    bool inCdnRoot = realFilePath.find(cdnRealPath) == 0; // There would be a match from index 0 to the end
    if (inCdnRoot) {
        newImagePath = "/cdn/" + filePath.substr(filePath.find_last_of("/") + 1);
    } else {
        // Copy the file as it is not at root then set the newImagePath
        std::string extension = newImagePath.substr(newImagePath.find_last_of("."));
        QUuid uuid = QUuid::createUuid();
        QString fileName = "/cdn/" + uuid.toString() + QString::fromStdString(extension);
        newImagePath = fileName.toStdString();

        fileName = QString::fromStdString(this->getCdnRoot()) + fileName;
        FILE *src = fopen(filePath.c_str(), "rb");
        if (src == NULL) {
            std::cerr << "Failed to read from "
                      << filePath
                      << std::endl;

            QMessageBox msgBox;
            msgBox.setText("There was an error copying the file to the cdn root (cannot read input).");
            msgBox.exec();
            return "";
        }

        FILE *dest = fopen(fileName.toStdString().c_str(), "wb");
        if (dest == NULL) {
            fclose(src);
            std::cerr << "Failed to write to "
                      << newImagePath
                      << std::endl;

            QMessageBox msgBox;
            msgBox.setText("There was an error copying the file to the cdn root (cannot write to output).");
            msgBox.exec();
            return "";
        }

        int c;
        while ((c = getc(src)) != EOF) {
            putc((char) c, dest);
        }

        fclose(src);
        fclose(dest);

        std::cout << "Saved a copy of the selected image to "
                  << fileName.toStdString()
                  << std::endl;
    }

    return newImagePath;
}

//==========
// Getters
//==========

std::string Configuration::getCdnRoot()
{
    return this->cdn_root;
}

std::string Configuration::getDbName()
{
    return this->db_name;
}

std::string Configuration::getDbPassword()
{
    return this->db_password;
}

std::string Configuration::getDbUserName()
{
    return this->db_username;
}

std::string Configuration::getDbHostAddress()
{
    return this->db_host_address;
}

int Configuration::getPort()
{
    return this->db_port;
}

void Configuration::setDbName(std::string val)
{
    this->db_name = val;
}

void Configuration::setDbPassword(std::string val)
{
    this->db_password = val;
}

void Configuration::setDbUserName(std::string val)
{
    this->db_username = val;
}

void Configuration::setDbHostAddress(std::string val)
{
    this->db_host_address = val;
}

void Configuration::setCdnRoot(std::string val)
{
    this->cdn_root = val;
}

void Configuration::setPort(int port)
{
    this->db_port = port;
}
