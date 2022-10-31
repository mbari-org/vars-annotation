package org.mbari.vars.ui.javafx.mlstage;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.mbari.imgfx.roi.Localization;
import org.mbari.imgfx.roi.RectangleView;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.javafx.shared.FilteredComboBoxDecorator;

public class LocalizationEditorController {

    private HBox root;
    private JFXCheckBox checkBox;
    private JFXComboBox<String> conceptComboBox;
    private final UIToolBox toolBox;
    private final Localization<RectangleView, ImageView> localization;

    public LocalizationEditorController(UIToolBox toolBox, Localization<RectangleView, ImageView> localization) {
        this.toolBox = toolBox;
        this.localization = localization;
        init();
    }

    private void init() {
        conceptComboBox = new JFXComboBox<>();
        new FilteredComboBoxDecorator<>(conceptComboBox, FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);
        conceptComboBox.setEditable(false);
        conceptComboBox.setValue(localization.getLabel());
        conceptComboBox.setOnKeyReleased(v -> {
            if (v.getCode() == KeyCode.ENTER) {
                String item = conceptComboBox.getValue();
                localization.setLabel(item);
            }
        });

        Color c = (Color) localization.getDataView().getView().getFill();
        Color nonOpaque = Color.color(c.getRed(), c.getGreen(), c.getBlue());
        checkBox.setCheckedColor(nonOpaque);
        conceptComboBox.setFocusColor(nonOpaque);

        checkBox.selectedProperty()
                .addListener((obs, oldv, newv) -> localization.setVisible(newv));
        checkBox.setSelected(true);

        root.getChildren().addAll(checkBox, conceptComboBox);

        loadComboBoxData();
    }

    private void loadComboBoxData() {

        toolBox.getServices()
                .getConceptService()
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
