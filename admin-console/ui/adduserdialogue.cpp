#include <QMessageBox>
#include <iostream>
#include "adduserdialogue.h"
#include "ui_adduserdialogue.h"

AddUserDialogue::AddUserDialogue(Configuration *config, QWidget *parent) :
    QDialog( parent),
    ui(new Ui::AddUserDialogue)
{
    ui->setupUi(this);
    this->setWindowTitle(("Add A User"));
    this->waitingForSave = false;

    this->passwordsValid = false;
    this->waitingForSave = false;
    this->config = config;

    ui->useType->insertItem(USER_CUSTOMER, tr("Customer"));
    ui->useType->insertItem(USER_KITCHEN, tr("Kitchen"));
    ui->useType->insertItem(USER_WAITER, tr("Waiter"));

    connect(ui->buttonBox, &QDialogButtonBox::accepted, this, &AddUserDialogue::addUser);
    connect(ui->userPasswordEdit, &QLineEdit::textChanged, this, &AddUserDialogue::inputChanged);
    connect(ui->userConfirmPasswordEdit, &QLineEdit::textChanged, this, &AddUserDialogue::inputChanged);
}

AddUserDialogue::~AddUserDialogue()
{
    delete ui;
}

void AddUserDialogue::inputChanged(QString newInput)
{
    if (ui->userPasswordEdit->text() == "") {
        ui->userPasswordStatus->setText(tr("Please insert a password"));
        this->passwordsValid = false;
    } else if (ui->userConfirmPasswordEdit->text() != ui->userPasswordEdit->text()) {
        ui->userPasswordStatus->setText(tr("The passwords do not match"));
        this->passwordsValid = false;
    } else {
        ui->userPasswordStatus->setText(tr("Password is all good"));
        this->passwordsValid = true;
    }
}

void AddUserDialogue::addUser()
{
    bool valid = config->isInputValid(ui->userPasswordEdit->text().toStdString())
                 && config->isInputValid(ui->userSnameEdit->text().toStdString())
                 && config->isInputValid(ui->userFnameEdit->text().toStdString())
                 && config->isInputValid(ui->userPhoneNoEdit->text().toStdString())
                 && config->isInputValid(ui->userEmailEdit->text().toStdString());
    if (this->passwordsValid && valid && !this->waitingForSave) {
        this->waitingForSave = true;

        User user = User(ui->userFnameEdit->text().toStdString(),
                         ui->userSnameEdit->text().toStdString(),
                         ui->userEmailEdit->text().toStdString(),
                         ui->userPhoneNoEdit->text().toStdString(),
                         ui->useType->currentIndex());
        if (config->addUser(user, ui->userPasswordEdit->text().toStdString())) {
            emit onAdd();
            emit close();
        } else {
            QApplication::beep();
            std::cerr << "Failed to add new user"
                      << std::endl;

            QMessageBox msgBox;
            msgBox.setText("There was an error, maybe the email or phone number are already taken, please try again.");
            msgBox.exec();
        }
        this->waitingForSave = false;
    } else {
        QApplication::beep();
    }
}

