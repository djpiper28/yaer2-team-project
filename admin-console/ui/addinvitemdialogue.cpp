#include <QMessageBox>
#include <iostream>
#include "addinvitemdialogue.h"
#include "ui_addinvitemdialogue.h"

AddInvItemDialogue::AddInvItemDialogue(Configuration *config, QWidget *parent) :
    QDialog(parent),
    ui(new Ui::AddInvItemDialogue)
{
    ui->setupUi(this);
    this->setWindowTitle(tr("Add An Inventory Item"));
    this->config = config;
    this->waitingForSave = false;
    connect(ui->buttonBox, &QDialogButtonBox::accepted, this, &AddInvItemDialogue::addItem);
}

AddInvItemDialogue::~AddInvItemDialogue()
{
    delete ui;
}

void AddInvItemDialogue::addItem()
{
    if (this->waitingForSave) {
        QApplication::beep();
    } else {
        this->waitingForSave = true;
        bool valid = this->config->isInputValid(ui->nameEdit->text().toStdString())
                     && ui->stockEdit->value() >= 0;
        if (!valid) {
            QApplication::beep();
            std::cerr << "Invalid input"
                      << std::endl;
        } else {
            InventoryItem item(ui->nameEdit->text().toStdString(),
                               ui->stockEdit->value());

            if (this->config->addInvItem(item)) {
                emit accept();
                emit onAdd();
            } else {
                QApplication::beep();
                std::cerr << "Failed to add new item"
                          << std::endl;

                QMessageBox msgBox;
                msgBox.setText("There was an error, please try again.");
                msgBox.exec();
            }
        }
        this->waitingForSave = false;
    }
}
