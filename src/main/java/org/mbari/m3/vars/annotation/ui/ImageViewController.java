package org.mbari.m3.vars.annotation.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.util.StringConverter;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.messages.SaveImageMsg;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.ImageReference;
import org.mbari.m3.vars.annotation.ui.shared.ImageStage;

import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-08-07T16:18:00
 */
public class ImageViewController {


    private final UIToolBox toolBox;
    private ObjectProperty<Annotation> annotation = new SimpleObjectProperty<>();
    private ImageView imageView;
    private BorderPane root;
    private ComboBox<ImageReference> comboBox;
    // TODO pull image stage into separate controller and add image annotation functions
    private ImageStage imageStage = new ImageStage();
    private ToolBar toolBar;

    public ImageViewController(UIToolBox toolBox) {
        this.toolBox = toolBox;

        imageStage.getScene()
                .getStylesheets()
                .addAll(toolBox.getStylesheets());
        imageStage.getScene()
                .setOnKeyPressed(keyEvent -> {
                    if (keyEvent.isMetaDown()) {
                        switch (keyEvent.getCode()) {
                            case S: saveImage(imageStage.getOwner()); break;
                            case W: imageStage.hide();
                        }
                    }
                });
    }

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
            imageStage.setOnCloseRequest(evt -> imageStage.close());
            imageView = new ImageView();
            imageView.setFitWidth(100);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.setCache(true);
            imageView.setOnMouseClicked(evt -> {
                if (evt.getClickCount() == 2) { // Pop out on double-click
                    ImageReference item = getComboBox().getSelectionModel().getSelectedItem();
                    Image image = new Image(item.getUrl().toExternalForm());
                    imageStage.setImage(image);
                    imageStage.show();
                }
            });
        }
        return imageView;
    }

    public BorderPane getRoot() {
        if (root == null) {
            root = new BorderPane();
            root.setTop(getComboBox());

            ImageView iv = getImageView();
            iv.fitWidthProperty().bind(root.widthProperty());
            iv.fitHeightProperty().bind(root.heightProperty());
            root.setCenter(iv);

            imageStage.getRoot().setTop(getToolBar());

        }
        return root;
    }

    private ToolBar getToolBar() {
        if (toolBar == null) {
            MaterialIconFactory iconFactory = MaterialIconFactory.get();
            Text icon = iconFactory.createIcon(MaterialIcon.SAVE, "30px");
            String tooltip = toolBox.getI18nBundle().getString("imageview.button.save");
            Button saveBtn = new JFXButton();
            saveBtn.setTooltip(new Tooltip(tooltip));
            saveBtn.setGraphic(icon);
            saveBtn.setOnAction(evt -> saveImage(imageStage.getOwner()));

            toolBar = new ToolBar(saveBtn);
        }
        return toolBar;
    }

    private void saveImage(Window owner) {
        ImageReference item = comboBox.getSelectionModel().getSelectedItem();
        if (item.getUrl() != null) {
            toolBox.getEventBus()
                    .send(new SaveImageMsg(item.getUrl(), owner));
        }
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
                    return object == null ? null : object.getDescription() + " [" + object.getFormat() + "]";
                }

                @Override
                public ImageReference fromString(String string) {
                    return null;
                }
            });

            comboBox.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((obs, oldv, newv) -> {
                        Image image = newv == null ? null : new Image(newv.getUrl().toExternalForm(), true);
                        getImageView().setImage(image);
                    });
            comboBox.setMaxWidth(Double.MAX_VALUE);
            //comboBox.setEditable(false);
        }
        return comboBox;
    }

}
