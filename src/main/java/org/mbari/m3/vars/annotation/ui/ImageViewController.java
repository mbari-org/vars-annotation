package org.mbari.m3.vars.annotation.ui;

import com.jfoenix.controls.JFXComboBox;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.ImageReference;

import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-08-07T16:18:00
 */
public class ImageViewController {

    private ObjectProperty<Annotation> annotation = new SimpleObjectProperty<>();
    private ImageView imageView;
    private BorderPane root;
    private ComboBox<ImageReference> comboBox;


    public void setAnnotation(Annotation annotation) {
        Platform.runLater(() -> {
            this.annotation.set(annotation);
            getComboBox().getItems().clear();
            if (annotation != null) {
                List<ImageReference> images = annotation.getImages();
                if (!images.isEmpty()) {
                    getComboBox().getItems().addAll(images);
                    getComboBox().getSelectionModel().select(0);
                }
            }
        });

    }

    public ImageView getImageView() {
        if (imageView == null) {
            imageView = new ImageView();
            imageView.setFitWidth(100);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setCache(true);
        }
        return imageView;
    }

    public BorderPane getRoot() {
        if (root == null) {
            root = new BorderPane();
            root.setTop(getComboBox());

            // TODO resizing is not working correctly in dock node
            ImageView iv = getImageView();

            iv.fitWidthProperty().bind(root.widthProperty());
            iv.fitHeightProperty().bind(root.heightProperty());
            root.setCenter(iv);

        }
        return root;
    }

    public ComboBox<ImageReference> getComboBox() {
        if (comboBox == null) {
            comboBox = new JFXComboBox<>();
            comboBox.setCellFactory(param ->  new ListCell<ImageReference>() {
                @Override
                protected void updateItem(ImageReference item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    }
                    else {
                        String text = item.getDescription() + " [" + item.getFormat() + "]";
                        setText(text);
                    }
                }
            });
            comboBox.setConverter(new StringConverter<ImageReference>() {
                @Override
                public String toString(ImageReference object) {
                    return object.getDescription() + " [" + object.getFormat() + "]";
                }

                @Override
                public ImageReference fromString(String string) {
                    return null;
                }
            });

            comboBox.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((obs, oldv, newv) -> {
                        Image image = newv == null ? null : new Image(newv.getUrl().toExternalForm());
                        getImageView().setImage(image);
                    });
            comboBox.setMaxWidth(Double.MAX_VALUE);
            //comboBox.setEditable(false);
        }
        return comboBox;
    }

}
