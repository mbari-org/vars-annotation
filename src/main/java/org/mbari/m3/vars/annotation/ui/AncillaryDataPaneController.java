package org.mbari.m3.vars.annotation.ui;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.AncillaryData;

import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-08-25T16:13:00
 */
public class AncillaryDataPaneController {

    private BorderPane root;
    private PropertySheet propertySheet;
    private final UIToolBox toolBox;

    public AncillaryDataPaneController(UIToolBox toolBox) {
        this.toolBox = toolBox;
    }

    public BorderPane getRoot() {
        if (root == null) {
            root = new BorderPane(getPropertySheet());
        }
        return root;
    }

    protected PropertySheet getPropertySheet() {
        if (propertySheet == null) {
            propertySheet = new PropertySheet();
        }
        return propertySheet;
    }

    private void setAncillaryData(AncillaryData data) {
        if (data != null) {
            ObservableList<PropertySheet.Item> items = BeanPropertyUtils.getProperties(data);
            Platform.runLater(() -> propertySheet.getItems().addAll(items));
        }
    }

    public void setAncillaryData(UUID observationUuid) {
        Platform.runLater(() -> getPropertySheet().getItems().clear());
        if (observationUuid != null) {
            toolBox.getServices()
                    .getAnnotationService()
                    .findAncillaryData(observationUuid)
                    .thenAccept(this::setAncillaryData);
        }
    }


}
