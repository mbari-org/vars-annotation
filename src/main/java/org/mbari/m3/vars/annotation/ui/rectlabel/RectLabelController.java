package org.mbari.m3.vars.annotation.ui.rectlabel;


import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Image;
import org.mbari.m3.vars.annotation.ui.shared.ImageViewExt;
import org.mbari.m3.vars.annotation.util.JFXUtilities;

/**
 * @author Brian Schlining
 * @since 2018-05-04T15:04:00
 */
public class RectLabelController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private BorderPane root;

    @FXML
    private StackPane imageStackPane;

    @FXML
    private ImageView imageView;

    @FXML
    private JFXButton refreshButton;

    @FXML
    private JFXButton deleteButton;

    @FXML
    private JFXListView<Image> imageReferenceListView;

    @FXML
    private JFXListView<Annotation> observationListView;

    private ImageViewExt imageViewExt;

    private ObservableList<Image> images = FXCollections.observableArrayList();
    private ObservableList<Annotation> imageAnnotations = FXCollections.observableArrayList();

    private UIToolBox toolBox;

    @FXML
    void initialize() {
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.fitHeightProperty().bind(imageStackPane.heightProperty());
        imageView.fitWidthProperty().bind(imageStackPane.widthProperty());
        imageViewExt = new ImageViewExt(imageView);

        imageReferenceListView.setItems(images);
        imageReferenceListView.getSelectionModel()
                .setSelectionMode(SelectionMode.SINGLE);
        imageReferenceListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldv, newv) -> setSelectedImage(newv));

        observationListView.getSelectionModel()
                .setSelectionMode(SelectionMode.SINGLE);
        observationListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldv, newv) -> setSelectedAnnotation(newv));
    }

    private void setToolBox(UIToolBox toolBox) {
        this.toolBox = toolBox;
    }

    //    public Point2D convertToStackPane(Point2D imagePoint) {
//        double s = imageViewExt.computeActualScale();
//        Point2D imageViewPoint = imagePoint.multiply(s);
//    }

    private void setSelectedAnnotation(Annotation annotation) {
        if (annotation == null) {
            imageView.setImage(null);
        }
        else {
            // TODO draw bounding box for selected annotation in different color
        }
    }

    private void setSelectedImage(Image image) {
        if (image == null) {
            observationListView.getItems().clear();
            imageView.setImage(null);
        }
        else {
            javafx.scene.image.Image jfxImage =
                    new javafx.scene.image.Image(image.getUrl().toExternalForm(),
                            true);
            imageView.setImage(jfxImage);
            // lookup annotations for that image
            toolBox.getServices()
                    .getAnnotationService()
                    .findByImageReference(image.getImageReferenceUuid())
                    .thenAccept(annotations -> JFXUtilities.runOnFXThread(() ->
                            observationListView.getItems().setAll(annotations)));

            // TODO Draw bounding boxes for any that are present in the annotations
        }
    }

    public BorderPane getRoot() {
        return root;
    }

    public ObservableList<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images.clear();
        this.images.addAll(images);
    }

    public static RectLabelController newInstance(UIToolBox toolBox) {
        final ResourceBundle i18n = Initializer.getToolBox().getI18nBundle();
        FXMLLoader loader = new FXMLLoader(RectLabelController.class
                .getResource("/fxml/RextLabel.fxml"), i18n);
        try {
            loader.load();
            RectLabelController controller = loader.getController();
            controller.setToolBox(toolBox);
            return controller;
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to load RectLabel from fxml");
        }
    }
}


