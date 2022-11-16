#pragma once
#include <QDialog>
#include "../model/configuration.h"
#include "../model/user.h"

namespace Ui
{
class ChangePasswordDialogue;
}

class ChangePasswordDialogue : public QDialog
{
    Q_OBJECT

public:
    explicit ChangePasswordDialogue(User user, Configuration *config, QWidget *parent = nullptr);
    ~ChangePasswordDialogue();

private:
    Ui::ChangePasswordDialogue *ui;
    User user;
    Configuration *config;
    bool waitingForSave;
    bool valid;
private slots:
    void inputChanged(QString newInput);
    void submit();
};

