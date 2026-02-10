package org.mbari.vars.annotation.ui.javafx.shared;


import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * @author Brian Schlining
 * @since 2014-12-05T09:35:00
 */
public class ImageStage extends Stage {

    private ImageView imageView;
    private ImageViewExt imageViewExt;

    public ImageStage() {
        initialize();
    }

    public ImageStage(StageStyle style) {
        super(style);
        initialize();
    }

    private void initialize() {
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);

        imageViewExt = new ImageViewExt(imageView);

        BorderPane root = new BorderPane(imageView);
        imageView.fitWidthProperty().bind(widthProperty());
        imageView.fitHeightProperty().bind(heightProperty());

        Scene scene = new Scene(root);
        scene.setFill(Color.BLACK);
        setScene(scene);
    }

    public void setImage(Image image) {
        imageView.setImage(image);
    }

    public BorderPane getRoot() {
        return (BorderPane) getScene().getRoot();
    }

    public Image getImage() {
        return imageView.getImage();
    }

    public double getImageScale() {
        return imageViewExt.computeActualScale();
    }

    public ImageView getImageView() {
        return imageView;
    }

    public Point2D convertToScene(Point2D imagePoint) {

        double s = imageViewExt.computeActualScale();
        Point2D imageViewPoint = imagePoint.multiply(s);
        return imageView.localToScene(imageViewPoint);

    }

    public Point2D convertToImage(Point2D scenePoint) {
        double s = imageViewExt.computeActualScale();
        Point2D imageViewPoint = imageView.sceneToLocal(scenePoint.getX(), scenePoint.getY());
        return imageViewPoint.multiply(1 / s);
    }

}

