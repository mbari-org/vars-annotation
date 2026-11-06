package org.mbari.vars.annotation.ui.javafx.mlstage;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
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
import org.mbari.vars.annotation.ui.util.ColorUtil;

public class LocalizationEditorController {

    private HBox root;
    private ComboBox<String> conceptComboBox;
    private final UIToolBox toolBox;
    private final Localization<RectangleView, ImageView> localization;
    private BooleanBinding highlighted;

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

        CheckBox checkBox = new CheckBox();
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
        highlighted = root.hoverProperty().or(conceptComboBox.showingProperty());

        // Show the concept in the same color as its box on the image
        applyConceptColor();
        localization.labelProperty().addListener((obs, oldv, newv) -> applyConceptColor());

        loadComboBoxData();
    }

    private void applyConceptColor() {
        // Style the button cell installed by FilteredComboBoxDecorator (the one that shows
        // the value) rather than replacing it. An inline style is used because it takes
        // precedence over the app's stylesheets, which setTextFill() does not.
        var buttonCell = conceptComboBox.getButtonCell();
        if (buttonCell != null) {
            var color = MLUtil.readableOn(MLUtil.colorFor(localization.getLabel()), MachineLearningStage.BACKGROUND);
            var hex = ColorUtil.toHex(color);
            buttonCell.setStyle("-fx-text-fill: " + hex + ";");
        }
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

    public Localization<RectangleView, ImageView> getLocalization() {
        return localization;
    }

    /**
     * True while the mouse is over this editor, or while its concept dropdown is open.
     * The dropdown is a separate popup window, so the editor loses hover as soon as the
     * mouse moves into it, which is exactly when the user is picking a concept.
     */
    public BooleanBinding highlightedProperty() {
        return highlighted;
    }

    public boolean isHovered() {
        return root.isHover();
    }
}
