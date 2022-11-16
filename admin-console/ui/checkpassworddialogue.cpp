#include "checkpassworddialogue.h"
#include "ui_checkpassworddialogue.h"
#include "../security.h"

CheckPasswordDialogue::CheckPasswordDialogue(User user, std::string passwordHash, std::string salt, QWidget *parent) :
    QDialog(parent),
    ui(new Ui::CheckPasswordDialogue)
{
    ui->setupUi(this);
    this->setWindowTitle(tr("Checking passsword for ") + QString::fromStdString(user.getFName() + " " + user.getSName()));
    this->user = user;
    this->salt = salt;
    this->passwordHash = passwordHash;

    connect(ui->passwordEdit, &QLineEdit::textChanged, this, &CheckPasswordDialogue::testPassword);
    emit testPassword("");
}

CheckPasswordDialogue::~CheckPasswordDialogue()
{
    delete ui;
}

void CheckPasswordDialogue::testPassword(QString query)
{
    std::string newPassword = query.toStdString();
    int status = 0;
    std::string hash = hashPassword(newPassword, this->salt, &status);

    if (status) {
        if (hash == this->passwordHash) {
            ui->passwordStatus->setText(tr("The passwords match"));
        } else {
            ui->passwordStatus->setText(tr("The passwords do not match"));
        }
    } else {
        ui->passwordStatus->setText(tr("Unable to hash the password"));
    }
}
