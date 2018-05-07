package org.mbari.m3.vars.annotation.ui.rectlabel;


import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.Image;
import org.mbari.m3.vars.annotation.ui.shared.ImageViewExt;
import org.mbari.m3.vars.annotation.util.FormatUtils;
import org.mbari.m3.vars.annotation.util.JFXUtilities;

/**
 * <pre>
 *     linkName: bounding box
 *     toConcept: self
 *     linkValue: {"x": 10,
 *                 "y": 20,
 *                 "width": 50,
 *                 "height": 20,
 *                 "image_reference_uuid": "512db2d7-7b57-4463-a44a-3acb36df0513",
 *                 "mime_type": "application/json}
 * </pre>
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

    /**
     * StackPane needs min size set to 0, 0 to resize correctly in
     * the BorderPane. I set this in the FXML
     */
    @FXML
    private StackPane imageStackPane;

    @FXML
    private HBox rightPane;

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

    @FXML
    private ScrollPane imageScrollPane;

    @FXML
    private ScrollPane observationScrollPane;

    @FXML
    private AnchorPane boxPane;

    private ImageViewExt imageViewExt;

    private ObservableList<Image> images = FXCollections.observableArrayList();
    private ObservableList<Annotation> imageAnnotations = FXCollections.observableArrayList();

    private Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private class BoundingBox {
        double x;
        double y;
        double width;
        double height;
    }

    private UIToolBox toolBox;

    @FXML
    void initialize() {

        GlyphsFactory gf = MaterialIconFactory.get();
        Text refreshIcon = gf.createIcon(MaterialIcon.REFRESH, "30px");
        refreshButton.setGraphic(refreshIcon);
        // Set refreshButton action in the stage controller

        Text deleteIcon = gf.createIcon(MaterialIcon.DELETE, "30px");
        deleteButton.setGraphic(deleteIcon);
        // Set deleteButton

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.fitHeightProperty().bind(imageStackPane.heightProperty());
        imageView.fitWidthProperty().bind(imageStackPane.widthProperty());
        imageViewExt = new ImageViewExt(imageView);
        rightPane.heightProperty().addListener((obs, oldv, newv) -> {
            imageScrollPane.setPrefHeight(newv.doubleValue());
            observationScrollPane.setPrefHeight(newv.doubleValue());
        });



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
        observationListView.setCellFactory(view ->
             new ListCell<Annotation>() {
                @Override
                protected void updateItem(Annotation item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item.getConcept());
                    }
                }
            });

        imageReferenceListView.setCellFactory(view ->
            new ListCell<Image>() {
                @Override
                protected void updateItem(Image item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        String text = "";
                        if (item.getRecordedTimestamp() != null) {
                            text = item.getRecordedTimestamp().toString();
                        }
                        else if (item.getElapsedTime() != null) {
                            text = FormatUtils.formatDuration(item.getElapsedTime());
                        }
                        else if (item.getTimecode() != null) {
                            text = item.getTimecode().toString();
                        }
                        setText(text);
                    }
                }
            });
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
                    .thenAccept(annotations -> JFXUtilities.runOnFXThread(() -> {
                        observationListView.getItems().clear();
                        observationListView.getItems().addAll(annotations);

                        // TODO Draw bounding boxes for any that are present in the annotations
                    }));

            Association association = new Association("bounding box",
                    "self",
                    "{\"x\": 10, \"y\":20, \"width\": 40, \"height\": 50}",
                    "application/json");
            drawAssociation(association, Color.CYAN);




        }
    }

    private void drawAssociation(Association association, Color color) {

        if (association.getLinkName().equalsIgnoreCase("bounding box")) {
            try {
                final Association a = association;
                Rectangle r = drawBoundingBox(association, null);
                r.setStroke(color);
                r.setStrokeWidth(2);
                r.setFill(null);
                imageStackPane.widthProperty().addListener((obs, oldv, newv) ->
                        drawBoundingBox(a, r));
                imageStackPane.heightProperty().addListener((obs, oldv, newv) ->
                        drawBoundingBox(a, r));
                boxPane.getChildren().add(r);
            }
            catch (Exception e) {
                // TODO log exectpion
                e.printStackTrace();
            }
        }

    }

    private Rectangle drawBoundingBox(final Association association, Rectangle r) {

        Bounds boundsInParent = imageView.boundsInParentProperty().get();

        double scale = imageViewExt.computeActualScale();
        BoundingBox bb = gson.fromJson(association.getLinkValue(), BoundingBox.class);
        double width = bb.width * scale;
        double height = bb.height * scale;
        double x = bb.x * scale + boundsInParent.getMinX();
        double y = bb.y * scale + boundsInParent.getMinY();
        if (r != null) {
            r.setX(x);
            r.setY(y);
            r.setWidth(width);
            r.setHeight(height);
        }
        else {
            r = new Rectangle(x, y, width, height);
        }
        return r;
    }

    public BorderPane getRoot() {
        return root;
    }

    public JFXButton getRefreshButton() {
        return refreshButton;
    }

    public JFXButton getDeleteButton() {
        return deleteButton;
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
                .getResource("/fxml/RectLabel.fxml"), i18n);
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


