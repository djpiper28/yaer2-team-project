#include <string.h>
#include <iostream>
#include <QApplication>
#include <QLocale>
#include <QTranslator>
#include <openssl/conf.h>
#include <openssl/evp.h>
#include <openssl/err.h>
#include "ui/mainwindow.h"
#include "tests/test.h"
#include "security.h"

static void cleanup()
{
    EVP_cleanup();
    CRYPTO_cleanup_all_ex_data();
    ERR_free_strings();
}

int main(int argc, char *argv[])
{
    // Init lib crypto
    std::cout << "Init openssl.. ";
    ERR_load_crypto_strings();
    OpenSSL_add_all_algorithms();
    //OPENSSL_config(NULL);
    // This is now deprecated so I guess I can ignore it. tbh the docs are poor

    // Randomise the seed
    initSeed();
    std::cout << "Done" << std::endl;

    // Execute tests if requested
    if (argc == 2) {
        if (strcmp(argv[1], "test") == 0) {
            int ret = test();
            cleanup();
            return ret;
        }
    }

    // Otherwise launch the application
    QApplication a(argc, argv);

    QTranslator translator;
    const QStringList uiLanguages = QLocale::system().uiLanguages();
    for (const QString &locale : uiLanguages) {
        const QString baseName = "admin-console_" + QLocale(locale).name();
        if (translator.load(":/i18n/" + baseName)) {
            a.installTranslator(&translator);
            break;
        }
    }

    MainWindow w;
    w.show();
    int ret = a.exec();
    cleanup();
    return ret;
}
