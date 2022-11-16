#pragma once
#include <QDialog>
#include "../model/configuration.h"

namespace Ui
{
class AddInvItemDialogue;
}

class AddInvItemDialogue : public QDialog
{
    Q_OBJECT

signals:
    void onAdd();
public:
    explicit AddInvItemDialogue(Configuration *config, QWidget *parent = nullptr);
    ~AddInvItemDialogue();
private slots:
    void addItem();
private:
    Ui::AddInvItemDialogue *ui;
    Configuration *config;
    bool waitingForSave;
};

