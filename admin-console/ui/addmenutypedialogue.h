#pragma once
#include <QDialog>
#include <QTimer>
#include <QGraphicsPixmapItem>
#include <QGraphicsScene>
#include "../model/configuration.h"

namespace Ui
{
class AddMenuTypeDialogue;
}

class AddMenuTypeDialogue : public QDialog
{
    Q_OBJECT

signals:
    void onAdd();
public:
    explicit AddMenuTypeDialogue(Configuration *config, QWidget *parent = nullptr);
    ~AddMenuTypeDialogue();
private slots:
    void addType();
    void resizeImage();
    void changeImage();
    void setImage();
private:
    Ui::AddMenuTypeDialogue *ui;
    Configuration *config;
    QTimer resizeTimer;
    QGraphicsPixmapItem *img;
    QGraphicsScene* scene;
    QImage image;
    bool waitingForSave;
};
