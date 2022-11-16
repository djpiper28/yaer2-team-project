#pragma once
#include <QDialog>
#include <QTimer>
#include <QGraphicsPixmapItem>
#include <QGraphicsScene>
#include "menutypetable.h"
#include "../model/configuration.h"

namespace Ui
{
class AddItemDialogue;
}

class AddItemDialogue : public QDialog
{
    Q_OBJECT

signals:
    void onAdd();
public:
    explicit AddItemDialogue(Configuration *config, QWidget *parent = nullptr);
    ~AddItemDialogue();
private slots:
    void addItem();
    void resizeImage();
    void changeImage();
    void setImage();
private:
    Ui::AddItemDialogue *ui;
    Configuration *config;
    QTimer resizeTimer;
    QGraphicsPixmapItem *img;
    QGraphicsScene* scene;
    QImage image;
    std::list<MenuType> types;
    MenuTypeTable *comboModel;
    bool waitingForSave;
};
