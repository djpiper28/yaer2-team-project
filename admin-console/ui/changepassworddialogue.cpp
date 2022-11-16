#include <QMessageBox>
#include "changepassworddialogue.h"
#include "ui_changepassworddialogue.h"
#include "../security.h"

ChangePasswordDialogue::ChangePasswordDialogue(User user, Configuration *config, QWidget *parent) :
    QDialog(parent),
    ui(new Ui::ChangePasswordDialogue)
{
    ui->setupUi(this);
    this->setWindowTitle(tr("Changing Password For ") + QString::fromStdString(user.getFName() + " " + user.getSName()));
    this->user = user;
    this->config = config;
    this->waitingForSave = false;
    this->valid = false;

    connect(ui->buttonBox, &QDialogButtonBox::accepted, this, &ChangePasswordDialogue::submit);
    connect(ui->passwordEdit, &QLineEdit::textChanged, this, &ChangePasswordDialogue::inputChanged);
    connect(ui->confirmPasswordEdit, &QLineEdit::textChanged, this, &ChangePasswordDialogue::inputChanged);
}

ChangePasswordDialogue::~ChangePasswordDialogue()
{
    delete ui;
}

void ChangePasswordDialogue::inputChanged(QString newInput)
{
    if (ui->passwordEdit->text() == "") {
        ui->passwordStatus->setText(tr("Please insert a password"));
        this->valid = false;
    } else if (ui->confirmPasswordEdit->text() != ui->passwordEdit->text()) {
        ui->passwordStatus->setText(tr("The passwords do not match"));
        this->valid = false;
    } else {
        ui->passwordStatus->setText(tr("Password is all good"));
        this->valid = true;
    }
}

void ChangePasswordDialogue::submit()
{
    if (this->waitingForSave || !this->valid) {
        QApplication::beep();
    } else {
        this->waitingForSave = true;
        bool res = this->config->updateUserPassword(this->user, ui->passwordEdit->text().toStdString());
        if (res) {
            emit close();
        } else {
            QMessageBox msgBox;
            msgBox.setText(tr("The password could not be updated."));
            msgBox.exec();
        }

        this->waitingForSave = false;
    }
}

