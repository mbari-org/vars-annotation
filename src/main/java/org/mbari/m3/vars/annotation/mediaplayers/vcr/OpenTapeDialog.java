package org.mbari.m3.vars.annotation.mediaplayers.vcr;


import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2018-03-26T14:55:00
 */
public class OpenTapeDialog extends Dialog<MediaParams> {

    private final MediaParamsPaneController controller;


    public OpenTapeDialog(ResourceBundle i18n) {
        controller = MediaParamsPaneController.newInstance();

        setTitle(i18n.getString("vcr.dialog.title"));
        setHeaderText(i18n.getString("vcr.dialog.header"));
        setContentText(i18n.getString("vcr.dialog.content"));

        getDialogPane().setContent(controller.getRoot());
        ButtonType ok = new ButtonType(i18n.getString("global.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType(i18n.getString("global.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(ok, cancel);

        setResultConverter(buttonType -> {
            MediaParams mediaParams = null;
            if (buttonType == ok && controller.getMediaParams().isPresent()) {
                mediaParams = controller.getMediaParams().get();
            }
            return mediaParams;
        });

    }

    public void refresh() {
        controller.refresh();
    }
}
