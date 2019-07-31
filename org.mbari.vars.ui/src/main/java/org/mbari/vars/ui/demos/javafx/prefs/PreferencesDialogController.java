package org.mbari.vars.ui.demos.javafx.prefs;


import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.text.Text;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.demos.javafx.Icons;

import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-08-09T12:50:00
 */
public class PreferencesDialogController {

    private final PreferencesPaneController paneController;
    private final UIToolBox toolBox;

    public PreferencesDialogController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        paneController = new PreferencesPaneController(toolBox);
    }

    public void show() {
        paneController.load();
        ResourceBundle i18n = toolBox.getI18nBundle();
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(i18n.getString("prefsdialog.title"));
        dialog.setHeaderText(i18n.getString("prefsdialog.header"));
//        Text settingsIcon = gf.createIcon(MaterialIcon.SETTINGS, "30px");
        Text settingsIcon = Icons.SETTINGS.standardSize();
        dialog.setGraphic(settingsIcon);
        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane()
                .setContent(paneController.getRoot());
        dialog.getDialogPane()
                .getStylesheets()
                .addAll(toolBox.getStylesheets());
        Optional<ButtonType> buttonType = dialog.showAndWait();
        buttonType.ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                paneController.save();
            }
        });
    }
}
