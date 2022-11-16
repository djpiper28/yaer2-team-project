#pragma once
#include <QDialog>
#include "../model/user.h"

namespace Ui
{
class CheckPasswordDialogue;
}

class CheckPasswordDialogue : public QDialog
{
    Q_OBJECT

public:
    explicit CheckPasswordDialogue(User user, std::string passwordHash, std::string salt, QWidget *parent = nullptr);
    ~CheckPasswordDialogue();
private:
    User user;
    std::string passwordHash;
    std::string salt;
    Ui::CheckPasswordDialogue *ui;
private slots:
    void testPassword(QString query);
};

