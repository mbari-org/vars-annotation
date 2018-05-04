package org.mbari.m3.vars.annotation.ui.rectlabel;


import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Image;
import org.mbari.m3.vars.annotation.ui.shared.ImageViewExt;

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

    @FXML
    void initialize() {
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.fitHeightProperty().bind(imageStackPane.heightProperty());
        imageView.fitWidthProperty().bind(imageStackPane.widthProperty());
        imageViewExt = new ImageViewExt(imageView);
    }

//    public Point2D convertToStackPane(Point2D imagePoint) {
//        double s = imageViewExt.computeActualScale();
//        Point2D imageViewPoint = imagePoint.multiply(s);
//    }


    public BorderPane getRoot() {
        return root;
    }
}


