package org.mbari.vars.ui.javafx.imgfx;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import mbarix4j.io.FileUtilities;
import mbarix4j.net.URLUtilities;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Image;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.javafx.mediadialog.MediaPaneController;
import org.mbari.vars.ui.util.FXMLUtils;
import org.mbari.vars.ui.util.URLUtils;

import java.net.URL;
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
    private ListView<?> annoListView;

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
    private Magnifier magnifier;

    @FXML
    void initialize() {
        imageListView.setCellFactory(new Callback<ListView<Image>, ListCell<Image>>() {
            @Override
            public ListCell<Image> call(ListView<Image> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Image item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText("");
                        }
                        else {
                            var s = URLUtils.filename(item.getUrl());
                            setText(s);
                        }
                    }
                };
            }
        });
    }

    private void postInitialize() {
        imageView = magnifier.getImageView();
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

    private void setSelectedAnnotation(Annotation annotation) {

    }

    public VBox getRoot() {
        return root;
    }

    public static IFXVarsPaneController newInstance(IFXToolBox toolBox, ImageView imageView) {
        ResourceBundle i18n = Initializer.getToolBox().getI18nBundle();
        var controller = FXMLUtils.newInstance(IFXVarsPaneController.class,
                "/fxml/IFXVarsPane.fxml",
                i18n);
        controller.toolBox = toolBox;
        controller.magnifier = new Magnifier(imageView);
        controller.postInitialize();
        return controller;
    }

}


