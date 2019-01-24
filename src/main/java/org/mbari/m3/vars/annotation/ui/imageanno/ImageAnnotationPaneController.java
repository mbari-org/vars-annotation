package org.mbari.m3.vars.annotation.ui.imageanno;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Image;
import org.mbari.m3.vars.annotation.ui.shared.ImageViewExt;

import java.net.URL;
import java.util.ResourceBundle;

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

    private ObservableList<LayerController> layerControllers = FXCollections.observableArrayList();
    private ImageViewExt imageViewExt;

    @FXML
    void initialize() {
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.fitHeightProperty().bind(stackPane.heightProperty());
        imageView.fitWidthProperty().bind(stackPane.widthProperty());
        imageViewExt = new ImageViewExt(imageView);
    }

    public StackPane getStackPane() {
        return stackPane;
    }

    public static final ImageAnnotationPaneController newInstance(UIToolBox toolBox) {
        final ResourceBundle i18n = toolBox.getI18nBundle();
        FXMLLoader loader = new FXMLLoader(ImageAnnotationPaneController.class.getResource("/fxml/ImageAnnotationPane.fxml"), i18n);
        try {
            loader.load();
            ImageAnnotationPaneController controller = loader.getController();
            controller.layerControllers
                    .addAll(new PointLayerController(toolBox, controller.getStackPane()));
            return controller;
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to load ImageAnnotationPane from fxml", e);
        }
    }

    public BorderPane getRoot() {
        return root;
    }


    public void setSelectedAnnotation(Annotation annotation) {

    }

    private void setSelectedImage(final Image image) {

    }


}
