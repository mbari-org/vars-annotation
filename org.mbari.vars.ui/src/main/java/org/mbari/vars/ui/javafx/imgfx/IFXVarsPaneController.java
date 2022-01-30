package org.mbari.vars.ui.javafx.imgfx;

import javafx.application.Platform;
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
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Image;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.util.FXMLUtils;
import org.mbari.vars.ui.util.URLUtils;

import java.net.URL;
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
    private Spinner<?> magnificationSpinner;

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
    private final List<String> localizedLinkValues = List.of(RoiBoundingBox.LINK_NAME,
            RoiLine.LINK_NAME, RoiMarker.LINK_NAME, RoiPolygon.LINK_NAME);

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
                    }
                };
            }
        });

        annoListView.setCellFactory(new Callback<ListView<Annotation>, ListCell<Annotation>>() {
            @Override
            public ListCell<Annotation> call(ListView<Annotation> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Annotation item, boolean empty) {
                        super.updateItem(item, empty);
                        setStyle(null);
                        if (item == null || empty) {
                            setText("");
                            setTooltip(null);
                        }
                        else {
                            var s = item.getConcept();
                            setText(s);
                            setTooltip(new Tooltip(s));
                            if (!isLocalized(item)) {
                                setStyle("ifx-localized-annotation");
                            }

                        }
                    }
                };
            }
        });
    }

    private void postInitialize() {

        toolBox.getUIToolBox()
                .getData()
                .getSelectedAnnotations();

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

        // Populate the filter combobox
        allImages.addListener((ListChangeListener<? super Image>) c -> {
                    var exts = allImages.stream()
                            .map(i -> URLUtils.extension(i.getUrl()))
                            .distinct()
                            .collect(Collectors.toList());
                    imageTypeComboBox.getItems().setAll(exts);
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
                        var i = new javafx.scene.image.Image(newv.getUrl().toExternalForm());
                        imageView.setImage(i);
                        setSelectedImage(newv);
                    }
                    else {
                        annoListView.getItems().clear();
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
        imageListView.setItems(filteredImages);
    }

    private void setSelectedImage(Image image) {
        toolBox.getUIToolBox()
                .getServices()
                .getAnnotationService()
                .findByImageReference(image.getImageReferenceUuid())
                .thenAccept(annos -> {
                    Platform.runLater(() -> {
                        annoListView.getItems().clear();
                        annoListView.getItems().addAll(annos);
                    });
                });
    }

    private void setSelectedAnnotation(Annotation annotation) {

    }

    private boolean isLocalized(Annotation annotation) {
        return annotation.getAssociations()
                .stream()
                .anyMatch(a -> localizedLinkValues.contains(a.getLinkName()));
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


