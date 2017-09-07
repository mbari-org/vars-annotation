package org.mbari.m3.vars.annotation.ui;

import com.jfoenix.controls.JFXComboBox;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.ImageReference;
import org.mbari.m3.vars.annotation.util.FXMLUtil;

import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-08-08T13:13:00
 * @deprecated Using ImageViewController for now.
 */
public class FramegrabPaneController {

    @FXML
    private ImageView imageView;

    @FXML
    private JFXComboBox<ImageReference> comboBox;

    @FXML
    private AnchorPane root;

    @FXML
    void initialize() {

        //
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        imageView.fitWidthProperty().bind(root.widthProperty());
        imageView.fitHeightProperty().bind(root.heightProperty());
//        imageView.parentProperty().addListener((obs, oldv, newv) -> {
//            newv.addEventHandler();
//        });

        comboBox.setCellFactory(param ->  new ListCell<ImageReference>() {
            @Override
            protected void updateItem(ImageReference item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                }
                else {
                    setText(asString(item));
                }
            }
        });
        comboBox.setConverter(new StringConverter<ImageReference>() {
            @Override
            public String toString(ImageReference object) {
                return asString(object);
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
                    imageView.setImage(image);
                });
        comboBox.setEditable(false);
    }

    private String asString(ImageReference imageReference) {
        return imageReference.getDescription() + " [" + imageReference.getFormat() + "]";
    }

    public AnchorPane getRoot() {
        return root;
    }

    public static FramegrabPaneController newInstance() {
        return FXMLUtil.newInstance(FramegrabPaneController.class, "/fxml/FramegrabPane.fxml");
    }

    public void setAnnotation(Annotation annotation) {
        comboBox.getItems().clear();
        if (annotation != null) {
            List<ImageReference> images = annotation.getImages();
            if (!images.isEmpty()) {
                comboBox.getItems().addAll(images);
                comboBox.getSelectionModel().select(1);
            }
        }
    }

}
