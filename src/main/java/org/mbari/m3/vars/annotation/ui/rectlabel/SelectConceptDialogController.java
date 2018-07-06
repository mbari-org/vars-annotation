package org.mbari.m3.vars.annotation.ui.rectlabel;

import com.jfoenix.controls.JFXComboBox;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Image;
import org.mbari.m3.vars.annotation.ui.shared.FilteredComboBoxDecorator;

import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2018-05-10T16:46:00
 */
public class SelectConceptDialogController {
    private JFXComboBox<String> conceptComboBox;
    private Image image;
    private final UIToolBox toolBox;
    private Dialog<String> dialog;

    public SelectConceptDialogController(UIToolBox toolBox) {
        this.toolBox = toolBox;
    }

    public Dialog<String> getDialog() {
        if (dialog == null) {
            dialog = new Dialog<>();
            ResourceBundle i18n = toolBox.getI18nBundle();
            // TODO set title and header
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getButtonTypes()
                    .addAll(ButtonType.OK, ButtonType.CANCEL);
            dialogPane.setContent(conceptComboBox);
            dialogPane.getStylesheets().addAll(toolBox.getStylesheets());
            dialog.setResultConverter(btnType -> {
                String concept = null;
                if (btnType == ButtonType.OK) {
                    concept = conceptComboBox.getValue();
                }
                return concept;
            });
        }
        return dialog;
    }

    public JFXComboBox<String> getConceptComboBox() {
        if (conceptComboBox == null) {
            conceptComboBox = new JFXComboBox<>();
            new FilteredComboBoxDecorator<>(conceptComboBox,
                    FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);
            conceptComboBox.setEditable(false);
            loadComboBoxData();
        }
        return conceptComboBox;
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

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
