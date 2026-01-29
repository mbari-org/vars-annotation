package org.mbari.vars.annotation.ui.javafx.imgfx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.mbari.imgfx.Autoscale;
import org.mbari.imgfx.imageview.ImageViewAutoscale;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.services.model.Image;
import org.mbari.vars.annotation.ui.Initializer;
import org.mbari.vars.annotation.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.annotation.ui.util.FXMLUtils;
import org.mbari.vars.annotation.ui.util.URLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

// Multiplie imageview on image: https://www.youtube.com/watch?v=bWQGIIzl0Vc
// Model server front end: https://adamant.tator.io:8082/
// https://stackoverflow.com/questions/44432343/javafx-scrollpane-in-splitpane-cannot-scroll
public class IFXVarsPaneController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private CheckBox addCommentCb;

    @FXML
    private CheckBox annoExistingCb;

    @FXML
    private ListView<Annotation> annoListView;

    @FXML
    private ComboBox<String> imageTypeComboBox;

    @FXML
    private ListView<Image> imageListView;

    @FXML
    private ImageView imageView;

    @FXML
    private Slider magnificationSlider;

    @FXML
    private Button mlButton;

    @FXML
    private TextField mlUrlTextField;

    @FXML
    private VBox root;

    private IFXToolBox toolBox;

    private String imageExt = "";
    private Autoscale<ImageView> copyAutoscale;
    private Autoscale<ImageView> originalAutoscale;

    private static final Logger log = LoggerFactory.getLogger(IFXVarsPaneController.class);

    @FXML
    void initialize() {
        imageView.setPreserveRatio(true);
        copyAutoscale = new ImageViewAutoscale(imageView);
        imageListView.setCellFactory(new Callback<ListView<Image>, ListCell<Image>>() {
            @Override
            public ListCell<Image> call(ListView<Image> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Image item, boolean empty) {
                        Platform.runLater(() -> {
                            super.updateItem(item, empty);
                            if (item == null || empty) {
                                setText("");
                                setTooltip(null);
                            }
                            else {
                                var s = URLUtils.filename(item.getUrl());
                                setText(s);
                                setTooltip(new Tooltip(s));
                            }
                        });
                    }
                };
            }
        });

//        annoListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        annoListView.setCellFactory(new Callback<ListView<Annotation>, ListCell<Annotation>>() {
            @Override
            public ListCell<Annotation> call(ListView<Annotation> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Annotation item, boolean empty) {
                        Platform.runLater(() -> {
                            super.updateItem(item, empty);
                            getStyleClass().remove("ifx-localized-annotation");
                            if (item == null || empty) {
                                setText("");
                                setTooltip(null);
                            }
                            else {
                                var s = item.getConcept();
                                setText(s);
                                setTooltip(new Tooltip(s));
                            }
                        });
                    }
                };
            }
        });
    }

    private void postInitialize() {

        var allImages = toolBox.getData().getImages();

        // Only show images of the desired type
        applyImageType();

        // When an image is selected in the list, show it in the editor pane
        imageListView.getSelectionModel()
                .getSelectedItems()
                .addListener((ListChangeListener<? super Image>) c -> {
                    var selected = imageListView.getSelectionModel().getSelectedItem();
                    toolBox.getData().setSelectedImage(selected);
                });

        // When an annotation is selected. Set its selection in UIToolbox
        annoListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldv, newv) -> {
                    if (newv != null) {
                        var event = new AnnotationsSelectedEvent(IFXVarsPaneController.this,
                                List.of(newv));
                        toolBox.getUIToolBox()
                                .getEventBus()
                                .send(event);
                    }
                });

        toolBox.getUIToolBox()
                .getEventBus()
                .toObserverable()
                .ofType(AnnotationsSelectedEvent.class)
                .subscribe(event -> {
                    if (event.getEventSource() != IFXVarsPaneController.this) {
                        setSelectedAnnotations(event.get());
                    }
                });

        // Populate the filter combobox
        allImages.addListener((ListChangeListener<? super Image>) c -> {
                    var exts = allImages.stream()
                            .map(i -> URLUtils.extension(i.getUrl()))
                            .distinct()
                            .collect(Collectors.toList());
                    Platform.runLater(() -> imageTypeComboBox.getItems().setAll(exts));
                });

        // When a filter is selected apply it
        imageTypeComboBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldv, newv) -> {
                    imageExt = newv;
                    applyImageType();
                });


        toolBox.getData()
                .selectedImageProperty()
                .addListener((obs, oldv, newv) -> {
                    if (newv != null) {
                        setSelectedImage(newv);
                    }
                    else {
                        Platform.runLater(() -> annoListView.getItems().clear());
                    }
                });

        originalAutoscale.getView().addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            var x = event.getSceneX();
            var y = event.getSceneY();
            var xy = new Point2D(x, y);
            var imageXy = originalAutoscale.sceneToUnscaled(xy);
            var magXy = copyAutoscale.unscaledToParent(imageXy);
//            var msg = String.format("scene=(%.1f,%.1f), original=(%.1f,%.1f), mag=(%.1f,%.1f)",
//                    x, y, imageXy.getX(), imageXy.getY(), magXy.getX(), magXy.getY());
//            System.out.println(msg);
            var magX = magXy.getX() - imageView.getFitWidth() / 2D;
            var magY = magXy.getY() - imageView.getFitHeight() / 2D;
            var viewPort = new Rectangle2D(magX, magY, imageView.getFitWidth(), imageView.getFitHeight());
            imageView.setViewport(viewPort);
        });

    }

    private boolean showImageType(Image image) {
        var imageUrl = image.getUrl();
        if (imageExt == null || imageExt.isEmpty() || imageExt.isBlank()) {
            return true;
        }
        return imageUrl.toExternalForm().endsWith(imageExt);
    }

    private void applyImageType() {
        var filteredImages = toolBox.getData().getImages()
                .filtered(this::showImageType);

        Platform.runLater(() -> imageListView.setItems(filteredImages));

    }

    private void setSelectedImage(Image image) {
        // Set image in magnified view
        var i = new javafx.scene.image.Image(image.getUrl().toExternalForm());
        imageView.setImage(i);
        var annos = LookupUtil.getAnnotationsForImage(toolBox, image);
        var obsList = FXCollections.observableArrayList(annos);
        Platform.runLater(() -> annoListView.setItems(obsList));
    }


    private void setSelectedAnnotations(Collection<Annotation> annotations) {

        LookupUtil.getImagesForAnnotations(toolBox, annotations)
                .stream()
                .filter(this::showImageType)
                .findFirst()
                .ifPresent(image -> toolBox.getData().setSelectedImage(image));

        var selectedAnno = annotations.size() == 1 ?
                annotations.iterator().next() : null;

        Platform.runLater(() -> {
            annoListView.getSelectionModel()
                    .select(selectedAnno);
        });

    }

    public VBox getRoot() {
        return root;
    }

    public static IFXVarsPaneController newInstance(IFXToolBox toolBox, Autoscale<ImageView> autoscale) {
        ResourceBundle i18n = Initializer.getToolBox().getI18nBundle();
        var controller = FXMLUtils.newInstance(IFXVarsPaneController.class,
                "/fxml/IFXVarsPane.fxml",
                i18n);
        controller.toolBox = toolBox;
        controller.originalAutoscale = autoscale;
        var width = controller.imageView.getFitWidth();
        var height = controller.imageView.getFitHeight();
        controller.imageView.setFitWidth(width);
        controller.imageView.setFitHeight(height);
        controller.postInitialize();
        return controller;
    }

}


