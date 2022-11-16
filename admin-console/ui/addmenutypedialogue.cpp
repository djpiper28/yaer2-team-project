#include <iostream>
#include <QFileDialog>
#include <QMessageBox>
#include "../model/menutype.h"
#include "addmenutypedialogue.h"
#include "ui_addmenutypedialogue.h"

AddMenuTypeDialogue::AddMenuTypeDialogue(Configuration *config, QWidget *parent) :
    QDialog(parent),
    ui(new Ui::AddMenuTypeDialogue)
{
    ui->setupUi(this);
    this->setWindowTitle(tr("Add A Menu Type"));
    this->config = config;
    this->img = nullptr;
    this->scene = new QGraphicsScene;
    this->waitingForSave = false;
    ui->imageView->setScene(scene);

    connect(ui->buttonBox, &QDialogButtonBox::accepted, this, &AddMenuTypeDialogue::addType);
    connect(ui->menuTypeImageEdit, &QLineEdit::textChanged, this, &AddMenuTypeDialogue::changeImage);
    connect(&this->resizeTimer, &QTimer::timeout, this, &AddMenuTypeDialogue::resizeImage);
    connect(ui->changeImage, &QPushButton::clicked, this, &AddMenuTypeDialogue::setImage);

    emit resizeImage();
}

AddMenuTypeDialogue::~AddMenuTypeDialogue()
{
    delete ui;
}

void AddMenuTypeDialogue::addType()
{
    if (this->waitingForSave) {
        QApplication::beep();
    } else {
        this->waitingForSave = true;
        bool valid = this->config->isInputValid(ui->menuTypeNameEdit->text().toStdString())
                     && this->config->isInputValid(ui->menuTypeImageEdit->text().toStdString())
                     && this->config->isInputValid(ui->menuTypeDescEdit->toPlainText().toStdString());
        if (!valid) {
            QApplication::beep();
            std::cerr << "Invalid input"
                      << std::endl;
        } else {
            MenuType type(ui->menuTypeNameEdit->text().toStdString(),
                          ui->menuTypeImageEdit->text().toStdString(),
                          ui->menuTypeDescEdit->toPlainText().toStdString());

            if (this->config->addMenuType(type)) {
                emit accept();
                emit onAdd();
            } else {
                QApplication::beep();
                std::cerr << "Failed to add new type"
                          << std::endl;

                QMessageBox msgBox;
                msgBox.setText(tr("There was an error, please try again."));
                msgBox.exec();
            }
        }
        this->waitingForSave = false;
    }
}

void AddMenuTypeDialogue::changeImage()
{
    std::string uri = this->config->getCdnRoot() + "/" + ui->menuTypeImageEdit->text().toStdString();
    std::cout << "Loading image "
              << uri
              << std::endl;
    FILE *f = fopen(uri.c_str(), "rb");
    if (f == NULL) {
        std::cerr << "Image is not present"
                  << std::endl;
    } else {
        fclose(f);
    }

    if (this->img != nullptr) {
        this->scene->removeItem(this->img);
    }
    this->image = QImage(QString::fromStdString(uri));
    this->img = new QGraphicsPixmapItem(QPixmap::fromImage(image));
    this->scene->addItem(this->img);
    this->img->setPos(0, 0);

    ui->imageView->show();
    this->resizeImage();
}

void AddMenuTypeDialogue::resizeImage()
{
    if (this->img != nullptr) {
        double w = this->image.width();
        double h = this->image.height();

        if (w == 0 || h ==0) {
            return;
        }

        double scale1 = ui->imageView->width() / w;
        double scale2 = ui->imageView->height() / h;

        // Min
        double scale = scale1;
        if (scale2 < scale1) {
            scale = scale2;
        }

        // Set scale
        this->img->setScale(scale);
        this->img->setPos(0, 0);
    }

    this->resizeTimer.start(500);
}

void AddMenuTypeDialogue::setImage()
{
    QFileDialog dialog = QFileDialog(this, tr("Select the image for the item"));
    dialog.setFileMode(QFileDialog::ExistingFile);
    dialog.setNameFilter(tr("Images (*.png *.xpm *.jpg *.jpeg *.webp)"));
    dialog.setViewMode(QFileDialog::Detail);

    if (!dialog.exec()) {
        return;
    }

    if (dialog.selectedFiles().size() != 1) {
        return;
    }

    std::string newImagePath = this->config->toCdnPath(dialog.selectedFiles().at(0).toStdString());
    if (newImagePath == "") {
        return;
    }

    ui->menuTypeImageEdit->setText(QString::fromStdString(newImagePath));
    emit changeImage();
    std::cout << "A new image was selected "
              << newImagePath
              << std::endl;
}

