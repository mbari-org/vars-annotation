package org.mbari.vars.annotation.ui.javafx;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;
import org.mbari.vars.annosaurus.sdk.r1.models.AncillaryData;
import org.mbari.vars.annotation.ui.UIToolBox;

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
            ObservableList<PropertySheet.Item> items = BeanPropertyUtils.getProperties(data)
                    .filtered(i -> i.getValue() != null);

            Platform.runLater(() -> getPropertySheet().getItems().addAll(items));
        }
    }

    public void setAncillaryData(UUID observationUuid) {
        Platform.runLater(() -> getPropertySheet().getItems().clear());
        if (observationUuid != null) {
            toolBox.getServices()
                    .annotationService()
                    .findAncillaryData(observationUuid)
                    .thenAccept(this::setAncillaryData);
        }
    }


}
