package org.mbari.m3.vars.annotation.ui.imageanno;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXToggleNode;
import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Image;
import org.mbari.m3.vars.annotation.ui.shared.ImageViewExt;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ImageAnnotationPaneController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private BorderPane root;

    @FXML
    private ToolBar toolbar;

    @FXML
    private StackPane stackPane;

    @FXML
    private ImageView imageView;

    @FXML
    private JFXComboBox<Image> comboBox;

    protected ObservableList<LayerController> layerControllers = FXCollections.observableArrayList();
    private ImageViewExt imageViewExt;

    @FXML
    void initialize() {
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.fitHeightProperty().bind(stackPane.heightProperty());
        imageView.fitWidthProperty().bind(stackPane.widthProperty());
        imageViewExt = new ImageViewExt(imageView);

        comboBox.setCellFactory(new Callback<ListView<Image>, ListCell<Image>>() {
            @Override
            public ListCell<Image> call(ListView<Image> param) {
                return new ListCell<Image>() {
                    @Override
                    protected void updateItem(Image item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.toString());
                        }
                        else {
                            setText(null);
                        }
                    }
                };
            }
        });

        comboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldv, newv) -> setSelectedImage(newv));


    }

    public ToolBar getToolbar() {
        return toolbar;
    }


    public static final ImageAnnotationPaneController newInstance(UIToolBox toolBox) {
        final ResourceBundle i18n = toolBox.getI18nBundle();
        FXMLLoader loader = new FXMLLoader(ImageAnnotationPaneController.class.getResource("/fxml/ImageAnnotationPane.fxml"), i18n);
        try {
            loader.load();
            ImageAnnotationPaneController controller = loader.getController();

            GlyphsFactory gf = MaterialIconFactory.get();
            ToggleGroup toggleGroup = new ToggleGroup();

            // Configure point layer controller
            PointLayerController pointLayerController = new PointLayerController(toolBox, controller.stackPane);
            pointLayerController.register(controller, toggleGroup);
            toggleGroup.selectedToggleProperty().addListener((obs, oldv, newv) -> {
                if (newv == null) {
                    controller.getRoot().setBottom(null);
                }
            });


            return controller;
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to load ImageAnnotationPane from fxml", e);
        }
    }




    public BorderPane getRoot() {
        return root;
    }


    public void setSelectedAnnotation(final Annotation annotation) {
        comboBox.getItems().clear();
        List<Image> images = annotation.getImages()
                .stream()
                .filter(imageReference -> imageReference.getUrl() != null)
                .map(imageReference -> new Image(annotation, imageReference))
                .sorted((a, b) -> a.toString().compareToIgnoreCase(b.toString()))
                .collect(Collectors.toList());
        comboBox.getItems().addAll(images);
        comboBox.getSelectionModel().select(0);
    }

    private void setSelectedImage(final Image image) {
        if (image == null) {
            imageView.setImage(null);
        }
        javafx.scene.image.Image fxImage = new javafx.scene.image.Image(
                image.getUrl().toExternalForm(),
                false);
        imageView.setImage(fxImage);


    }


}
