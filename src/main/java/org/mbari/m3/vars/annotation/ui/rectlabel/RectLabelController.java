package org.mbari.m3.vars.annotation.ui.rectlabel;


import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import java.net.URL;
import java.util.*;

import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.control.Dialog;
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
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.Command;
import org.mbari.m3.vars.annotation.commands.CreateAnnotationAtIndexCmd;
import org.mbari.m3.vars.annotation.commands.CreateAssociationsCmd;
import org.mbari.m3.vars.annotation.events.AnnotationsChangedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsRemovedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.Image;
import org.mbari.m3.vars.annotation.ui.shared.ImageViewExt;
import org.mbari.m3.vars.annotation.util.FormatUtils;
import org.mbari.m3.vars.annotation.util.JFXUtilities;
import org.mbari.vcr4j.VideoIndex;

/**
 * <pre>
 *     linkName: bounding box
 *     toConcept: self
 *     linkValue: {"x": 10,
 *                 "y": 20,
 *                 "width": 50,
 *                 "height": 20,
 *                 "image_reference_uuid": "512db2d7-7b57-4463-a44a-3acb36df0513"}
 *      mimeType: application/json
 * </pre>
 *
 * TODO kyra wants to be able to select and delete multiple bounding boxes
 * at one time.
 * @author Brian Schlining
 * @since 2018-05-04T15:04:00
 */
public class RectLabelController {

    private static final Color SELECTED_COLOR = Color.ORANGE;
    private static final Color NOT_SELECTED_COLOR = Color.CYAN;
    public static final String LINK_NAME = "bounding box";
    public static final String TO_CONCEPT = Association.VALUE_SELF;
    public static final String MIME_TYPE = "application/json";


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
    private ObservableList<Annotation> allAnnotations = FXCollections.observableArrayList();
    private ObservableList<Annotation> selectedAnnotations = FXCollections.observableArrayList();

    private Collection<BoundingBoxNode> boundingBoxNodes = new ArrayList<>();

    private Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private BoundingBoxCreator boundingBoxCreator;
    private SelectConceptDialogController dialogController;

    private class BoundingBox {
        double x;
        double y;
        double width;
        double height;
    }

    private class BoundingBoxNode {
        Shape boundingBox;
        ChangeListener<? super Number> changeListener;
        Association association;

        public BoundingBoxNode(Shape boundingBox,
                               ChangeListener<? super Number> changeListener,
                               Association association) {
            this.boundingBox = boundingBox;
            this.changeListener = changeListener;
            this.association = association;
        }
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
        deleteButton.setDisable(true);

        // Set deleteButton

        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.fitHeightProperty().bind(imageStackPane.heightProperty());
        imageView.fitWidthProperty().bind(imageStackPane.widthProperty());
        imageViewExt = new ImageViewExt(imageView);

        boxPane.prefHeightProperty().bind(imageStackPane.heightProperty());
        boxPane.prefWidthProperty().bind(imageStackPane.widthProperty());

        rightPane.heightProperty().addListener((obs, oldv, newv) -> {
            imageScrollPane.setPrefHeight(newv.doubleValue());
            observationScrollPane.setPrefHeight(newv.doubleValue());
            observationListView.setPrefHeight(newv.doubleValue());
            imageReferenceListView.setPrefHeight(newv.doubleValue());
        });

        imageReferenceListView.setItems(images);
        imageReferenceListView.getSelectionModel()
                .setSelectionMode(SelectionMode.SINGLE);
        imageReferenceListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldv, newv) -> setSelectedImage(newv));

        observationListView.getSelectionModel()
                .setSelectionMode(SelectionMode.MULTIPLE);
        observationListView.setItems(allAnnotations);
        observationListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldv, newv) -> {
                    ArrayList<Annotation> items = new ArrayList<>(observationListView
                            .getSelectionModel().getSelectedItems());
                    AnnotationsSelectedEvent selected = new AnnotationsSelectedEvent(RectLabelController.this, items);
                    toolBox.getEventBus()
                            .send(selected);
                    setSelectedAnnotations(items);
                });
        observationListView.setCellFactory(view ->
             new ListCell<Annotation>() {
                @Override
                protected void updateItem(Annotation item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item.getConcept());
                    }
                    else {
                        setText(null);
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

    public Map<Association, UUID> getSelectedBoundingBoxAssociations() {
        ArrayList<Annotation> selectedAnnotations = new ArrayList<>(observationListView.getSelectionModel()
                .getSelectedItems());

        Map<Association, UUID> map = new HashMap<>();
        for (Annotation a : selectedAnnotations) {
            for (Association as : a.getAssociations()) {
                if (as.getLinkName().equalsIgnoreCase(LINK_NAME)) {
                    map.put(as, a.getObservationUuid());
                }
            }
        }

        return map;
    }

    public void handleAnnotationsSelectedEvent(AnnotationsSelectedEvent event) {
        if (event.getEventSource() != RectLabelController.this) {
            observationListView.getSelectionModel().clearSelection();
            imageReferenceListView.getSelectionModel().clearSelection();
        }
    }

    private void setToolBox(UIToolBox toolBox) {
        this.toolBox = toolBox;
        dialogController = new SelectConceptDialogController(toolBox);
        toolBox.getEventBus()
                .toObserverable()
                .ofType(BoundingBoxCreatedEvent.class)
                .subscribe(this::handleBoundingBoxEvent);

        toolBox.getEventBus()
                .toObserverable()
                .ofType(AnnotationsChangedEvent.class)
                .subscribe(this::handleAnnotationsChangedEvent);

        toolBox.getEventBus()
                .toObserverable()
                .ofType(AnnotationsRemovedEvent.class)
                .subscribe(this::handleAnnotationsRemovedEvent);
    }


    private void setSelectedAnnotations(Collection<Annotation> annotations) {
        removeResizeListeners();
        deleteButton.setDisable(annotations == null || annotations.isEmpty());
        Image image = imageReferenceListView.getSelectionModel().getSelectedItem();
        if ((annotations == null || annotations.isEmpty()) && image != null) {
            boundingBoxCreator.setDisable(false);
        }
        else {

            // Split select vs non-selected associations
            FilteredList<Annotation> selected = allAnnotations.filtered(annotations::contains);
            selectedAnnotations.clear();
            selectedAnnotations.addAll(selected);
            FilteredList<Annotation> notSelected = allAnnotations.filtered(a -> !annotations.contains(a));
            drawAnnotations(selected, SELECTED_COLOR);
            drawAnnotations(notSelected, NOT_SELECTED_COLOR);

            //System.out.println("SELECTED " + selected.size() + " --- NOT SELECTED = " + notSelected.size());
            boolean disable = selected.size() != 1 || selected.get(0)
                    .getAssociations()
                    .stream()
                    .anyMatch(a -> a.getLinkName().equalsIgnoreCase(LINK_NAME));
            boundingBoxCreator.setDisable(disable);

        }
    }

    private void removeResizeListeners() {
        ArrayList<BoundingBoxNode> nodes = new ArrayList<>(boundingBoxNodes);
        boundingBoxNodes.clear();
        nodes.forEach(node -> {
            imageStackPane.widthProperty().removeListener(node.changeListener);
            imageStackPane.heightProperty().removeListener(node.changeListener);
            boxPane.getChildren().remove(node.boundingBox);
        });
    }

    private void setSelectedImage(Image image) {

        JFXUtilities.runOnFXThread(() -> {
            removeResizeListeners();
            allAnnotations.clear();
            if (image == null) {
                imageView.setImage(null);
            }
            else {
                javafx.scene.image.Image jfxImage =
                        new javafx.scene.image.Image(image.getUrl().toExternalForm(),
                                true);
                imageView.setImage(jfxImage);
                // lookup allAnnotations for that image

                toolBox.getServices()
                        .getAnnotationService()
                        .findByImageReference(image.getImageReferenceUuid())
                        .thenAccept(annotations -> JFXUtilities.runOnFXThread(() -> {
                            this.allAnnotations.addAll(annotations);
                            drawAnnotations(annotations, NOT_SELECTED_COLOR);
//                            if (annotations.size() == 1) {
//                                observationListView.getSelectionModel().select(annotations.get(0));
//                            }
                        }));

                Random r = new Random();
                int x = r.nextInt(1000);
                int y = r.nextInt(1000);
                int width = r.nextInt(100) + 10;
                int height = r.nextInt(100) + 10;
                String s = "{\"x\": " + x + ", \"y\": " + y +
                        ", \"width\": " + width + ", \"height\": " + height + "}";
                Association association = new Association("bounding box",
                        "self",
                        s,
                        "application/json");
                drawAssociation(association, Color.CYAN);

            }
        });
    }

    private void drawAnnotations(List<Annotation> annotations, Color color) {
        annotations.stream()
                .flatMap(anno -> anno.getAssociations().stream())
                .forEach(assoc -> drawAssociation(assoc, color));
    }



    /**
     * Call on JavaFX Thread
     * @param association
     * @param color
     */
    private void drawAssociation(Association association, Color color) {

        if (association.getLinkName().equalsIgnoreCase("bounding box")) {
            try {
                final Association a = association;
                Rectangle r = drawBoundingBox(association, null);
                r.setStroke(color);
                r.setStrokeWidth(2);
                r.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.1));
                // TODO remove listeners when new image is selected.
                ChangeListener<? super Number> listener = (obs, oldv, newv) ->
                        drawBoundingBox(a, r);
                boundingBoxNodes.add(new BoundingBoxNode(r, listener, association));
                imageStackPane.widthProperty().addListener(listener);
                imageStackPane.heightProperty().addListener(listener);
                boxPane.getChildren().add(r);
            }
            catch (Exception e) {
                // TODO log exectpion
                e.printStackTrace();
            }
        }
    }

    private void handleBoundingBoxEvent(BoundingBoxCreatedEvent event) {
        Image image = imageReferenceListView.getSelectionModel()
                .getSelectedItem();
        if (image != null) {
            Shape shape = event.getShape();

            if (shape instanceof Rectangle) {
                handleRectangle(event, image);
            }
        }
    }

    private void handleAnnotationsChangedEvent(AnnotationsChangedEvent event) {
        handleAnnotationChange(event.get());
    }

    private void handleAnnotationsRemovedEvent(AnnotationsRemovedEvent event) {
        handleAnnotationChange(event.get());
    }

    private void handleAnnotationChange(Collection<Annotation> annotations) {
        Image image = imageReferenceListView.getSelectionModel()
                .getSelectedItem();
        if (image != null) {
            boolean needsUpdate = annotations.stream()
                    .anyMatch(a -> a.getImagedMomentUuid().equals(image.getImagedMomentUuid()));
            if (needsUpdate) {
                setSelectedImage(image);
            }
        }
    }

    private Association rectangleToAssociation(Rectangle shape, Image image) {
        Bounds bounds = imageView.getBoundsInParent();
        double scale = imageViewExt.computeActualScale();

        // ImageView coords
        double viewX = shape.getX() - bounds.getMinX();
        double viewY = shape.getY() - bounds.getMinY();

        // Annotation coords (in original image coordinate space)
        double annoX = viewX / scale;
        double annoY = viewY / scale;

        double width = shape.getWidth() / scale;
        double height = shape.getHeight() / scale;

        return newAssociation(annoX, annoY, width, height, image.getImageReferenceUuid());
    }


    private void handleRectangle(BoundingBoxCreatedEvent event,  Image image) {

        Annotation selectedItem = observationListView.getSelectionModel()
                .getSelectedItem();


        Association association = rectangleToAssociation((Rectangle) event.shape, image);

        if (selectedItem != null) {
            Command cmd = new CreateAssociationsCmd(association, Collections.singletonList(selectedItem));
            toolBox.getEventBus().send(cmd);
        }
        else {
            // Show Dialog to add annotations
            Dialog<String> dialog = dialogController.getDialog();
            Optional<String> opt = dialog.showAndWait();
            // Create annotation with the association
            opt.ifPresent(concept -> {
                VideoIndex videoIndex = new VideoIndex(Optional.ofNullable(image.getRecordedTimestamp()),
                        Optional.ofNullable(image.getElapsedTime()),
                        Optional.ofNullable(image.getTimecode()));
                Command cmd = new CreateAnnotationAtIndexCmd(videoIndex, concept, association);
                toolBox.getEventBus().send(cmd);
            });
        }
        drawAssociation(association, SELECTED_COLOR);
    }

    private Association newAssociation(double x,
                                       double y,
                                       double width,
                                       double height,
                                       UUID imageReferenceUuid) {
        String linkValue = "{\"x\": " + x
                + ", \"y\": " + y +
                ", \"width\": " + width
                + ", \"height\": " + height
                + ", \"image_reference_uuid\": \"" + imageReferenceUuid.toString() + "\"}";
        return new Association(LINK_NAME, TO_CONCEPT, linkValue, MIME_TYPE);
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
        JFXUtilities.runOnFXThread(() -> {
            this.images.clear();
            this.images.addAll(images);
        });
    }

    public static RectLabelController newInstance(UIToolBox toolBox) {
        final ResourceBundle i18n = Initializer.getToolBox().getI18nBundle();
        FXMLLoader loader = new FXMLLoader(RectLabelController.class
                .getResource("/fxml/RectLabel.fxml"), i18n);
        try {
            loader.load();
            RectLabelController controller = loader.getController();
            controller.setToolBox(toolBox);
            controller.boundingBoxCreator = new BoundingBoxCreator(controller.boxPane,
                    toolBox.getEventBus());

            return controller;
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to load RectLabel from fxml", e);
        }
    }
}


