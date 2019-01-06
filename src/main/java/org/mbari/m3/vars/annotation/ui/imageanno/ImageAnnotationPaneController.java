package org.mbari.m3.vars.annotation.ui.imageanno;

import com.jfoenix.controls.JFXComboBox;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Image;
import org.mbari.m3.vars.annotation.ui.shared.ImageViewExt;

import java.net.URL;
import java.util.*;
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

    private ToggleGroup toggleGroup = new ToggleGroup();

    protected ObservableList<LayerController> layerControllers = FXCollections.observableArrayList();
    private Map<LayerController, ChangeListener<? super Boolean>> changeListenerMap = new HashMap<>();
    private Data data;

    @FXML
    void initialize() {
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.fitHeightProperty().bind(stackPane.heightProperty());
        imageView.fitWidthProperty().bind(stackPane.widthProperty());
        ImageViewExt imageViewExt = new ImageViewExt(imageView);
        data = new Data(stackPane, imageViewExt);

        // If not toggle button is selected do not show toolbar on bottom
        toggleGroup.selectedToggleProperty().addListener((obs, oldv, newv) -> {
            if (newv == null) {
                root.setBottom(null);
            }
        });

        // Combobox that displays Image as string
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

        // Add/remove toggle bottons for layers as they are addeded/removed
        layerControllers.addListener((ListChangeListener<LayerController>) c -> {
            ObservableList<Node> items = getToolbar().getItems();
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList()
                            .forEach(this::addLayerController);
                }
                if (c.wasRemoved()) {
                    c.getRemoved()
                            .forEach(this::removeLayerController);
                }
            }

        });


    }

    private void addLayerController(LayerController layerController) {
        ToggleButton enableButton = layerController.getEnableButton();
        enableButton.setToggleGroup(toggleGroup);
        ChangeListener<? super Boolean> changeListener = (obs, oldv, newv) -> {
            layerController.setDisable(!newv);
            if (newv) {
                getRoot().setBottom(layerController.getToolBar());
                data.setLayerController(layerController);
            }
        };
        enableButton.selectedProperty().addListener(changeListener);
        changeListenerMap.put(layerController, changeListener);
        toolbar.getItems().add(enableButton);
    }

    private void removeLayerController(LayerController layerController) {
        layerController.setDisable(true);
        stackPane.getChildren().remove(layerController.getRoot());
        ToggleButton enableButton = layerController.getEnableButton();
        toolbar.getItems().remove(enableButton);
        enableButton.setToggleGroup(null);
        ChangeListener<? super Boolean> changeListener = changeListenerMap.get(layerController);
        if (changeListener != null) {
            enableButton.selectedProperty().removeListener(changeListener);
            changeListenerMap.remove(layerController);
        }
        if (layerController == data.getLayerController()) {
            data.setLayerController(null);
        }
    }

    public ToolBar getToolbar() {
        return toolbar;
    }


    public static final ImageAnnotationPaneController newInstance(UIToolBox toolBox) {
        final ResourceBundle i18n = toolBox.getI18nBundle();
        FXMLLoader loader = new FXMLLoader(ImageAnnotationPaneController.class.getResource("/fxml/ImageAnnotationPane.fxml"), i18n);
        try {
            loader.load();
            return loader.getController();
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to load ImageAnnotationPane from fxml", e);
        }
    }


    /**
     * @return A read-only view of the LayerControllers list
     */
    public List<LayerController> getLayerControllers() {
        return layerControllers;
    }

    public BorderPane getRoot() {
        return root;
    }

    /**
     * The center pane of the root (BorderPane). It's where the image is displayed
     * @return
     */
    public StackPane getStackPane() {
        return stackPane;
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
        data.setImage(image);
    }

    public Data getData() {
        return data;
    }
}
