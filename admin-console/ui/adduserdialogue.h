#pragma once
#include <QDialog>
#include "../model/configuration.h"

namespace Ui
{
class AddUserDialogue;
}

class AddUserDialogue : public QDialog
{
    Q_OBJECT

signals:
    void onAdd();
public:
    explicit AddUserDialogue(Configuration *config, QWidget *parent = nullptr);
    ~AddUserDialogue();
private slots:
    void inputChanged(QString newInput);
    void addUser();
private:
    Ui::AddUserDialogue *ui;
    Configuration *config;
    bool waitingForSave;
    bool passwordsValid;
};
