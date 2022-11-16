#include <QMessageBox>
#include <iostream>
#include <QFileDialog>
#include <QDialogButtonBox>
#include "additemdialogue.h"
#include "ui_additemdialogue.h"

AddItemDialogue::AddItemDialogue(Configuration *config, QWidget *parent) :
    QDialog(parent),
    ui(new Ui::AddItemDialogue)
{
    ui->setupUi(this);
    this->setWindowTitle(tr("Add A Menu Item"));
    this->config = config;
    this->img = nullptr;
    this->scene = new QGraphicsScene;
    this->waitingForSave = false;
    ui->itemImage->setScene(scene);

    connect(ui->buttonBox, &QDialogButtonBox::accepted, this, &AddItemDialogue::addItem);
    connect(ui->itemImageUriEdit, &QLineEdit::textChanged, this, &AddItemDialogue::changeImage);
    connect(&this->resizeTimer, &QTimer::timeout, this, &AddItemDialogue::resizeImage);
    connect(ui->changeImage, &QPushButton::clicked, this, &AddItemDialogue::setImage);

    this->types = this->config->getMenuTypes();
    this->comboModel = new MenuTypeTable(this->types, config, true, parent);
    ui->itemTypeEdit->setModel(this->comboModel);
    emit ui->itemTypeEdit->setCurrentIndex(0);
    emit resizeImage();
}

AddItemDialogue::~AddItemDialogue()
{
    delete ui;
    delete comboModel;
}

void AddItemDialogue::addItem()
{
    if (this->waitingForSave) {
        QApplication::beep();
    } else {
        this->waitingForSave = true;
        bool valid = this->config->isInputValid(ui->itemNameEdit->text().toStdString())
                     && this->config->isInputValid(ui->itemDescEdit->toPlainText().toStdString())
                     && this->config->isInputValid(ui->itemImageUriEdit->text().toStdString());
        if (!valid) {
            QApplication::beep();
            std::cerr << "Invalid input"
                      << std::endl;
        } else {
            MenuType type;
            int i = 0;
            for (MenuType t : this->types) {
                if (i == ui->itemTypeEdit->currentIndex()) {
                    type = t;
                    break;
                }

                i++;
            }

            MenuItem item(ui->itemNameEdit->text().toStdString(),
                          ui->itemDescEdit->toPlainText().toStdString(),
                          ui->itemImageUriEdit->text().toStdString(),
                          type.getUUID(),
                          ui->itemPriceEdit->value(),
                          ui->itemActiveEdit->isChecked(),
                          this->config,
                          ui->itemPrepTimeEdit->value());

            if (this->config->addMenuItem(item)) {
                emit accept();
                emit onAdd();
            } else {
                QApplication::beep();
                std::cerr << "Failed to add new item"
                          << std::endl;

                QMessageBox msgBox;
                msgBox.setText(tr("There was an error, please try again."));
                msgBox.exec();
            }
        }
        this->waitingForSave = false;
    }
}

void AddItemDialogue::changeImage()
{
    std::string uri = this->config->getCdnRoot() + "/" + ui->itemImageUriEdit->text().toStdString();
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

    ui->itemImage->show();
    this->resizeImage();
}

void AddItemDialogue::resizeImage()
{
    if (this->img != nullptr) {
        double w = this->image.width();
        double h = this->image.height();

        if (w == 0 || h ==0) {
            return;
        }

        double scale1 = ui->itemImage->width() / w;
        double scale2 = ui->itemImage->height() / h;

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

void AddItemDialogue::setImage()
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

    ui->itemImageUriEdit->setText(QString::fromStdString(newImagePath));
    emit changeImage();
    std::cout << "A new image was selected "
              << newImagePath
              << std::endl;
}
