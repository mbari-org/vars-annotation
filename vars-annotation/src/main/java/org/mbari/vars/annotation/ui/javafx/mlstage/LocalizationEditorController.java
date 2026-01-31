package org.mbari.vars.annotation.ui.javafx.mlstage;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import org.mbari.imgfx.roi.Localization;
import org.mbari.imgfx.roi.RectangleView;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.javafx.shared.FilteredComboBoxDecorator;

public class LocalizationEditorController {

    private HBox root;
    private CheckBox checkBox;
    private ComboBox<String> conceptComboBox;
    private final UIToolBox toolBox;
    private final Localization<RectangleView, ImageView> localization;

    public LocalizationEditorController(UIToolBox toolBox, Localization<RectangleView, ImageView> localization) {
        this.toolBox = toolBox;
        this.localization = localization;
        init();
    }

    private void init() {
        conceptComboBox = new ComboBox<>();
        new FilteredComboBoxDecorator<>(conceptComboBox, FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);
        conceptComboBox.setEditable(false);
        conceptComboBox.setValue(localization.getLabel());
        conceptComboBox.setOnKeyReleased(v -> {
            if (v.getCode() == KeyCode.ENTER) {
                String item = conceptComboBox.getValue();
                localization.setLabel(item);
            }
        });

        checkBox = new CheckBox();
//        Color c = (Color) localization.getDataView().getView().getFill();
//        Color nonOpaque = Color.color(c.getRed(), c.getGreen(), c.getBlue());
//        checkBox.setCheckedColor(nonOpaque);
//        conceptComboBox.focusedProperty().addListener((obs, oldv, newv) -> {
//            if (newv) {
//                conceptComboBox.setBorder(new Border(new BorderStroke(nonOpaque, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
//            }
//            else {
//                conceptComboBox.setBorder(null);
//            }
//        });

        checkBox.selectedProperty()
                .addListener((obs, oldv, newv) -> localization.setVisible(newv));
        checkBox.setSelected(true);

        root = new HBox();
        root.getChildren().addAll(checkBox, conceptComboBox);

        loadComboBoxData();
    }

    private void loadComboBoxData() {

        toolBox.getServices()
                .conceptService()
                .findAllNames()
                .thenAccept(names -> {
                    FilteredList<String> cns = new FilteredList<>(FXCollections.observableArrayList(names));
                    Platform.runLater(() -> conceptComboBox.setItems(cns));
                });
    }

    public HBox getRoot() {
        return root;
    }
}
