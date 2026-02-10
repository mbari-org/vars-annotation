package org.mbari.vars.annotation.ui.mediaplayers.ships;


import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-12-21T09:58:00
 */
public class OpenRealTimeDialog extends Dialog<MediaParams> {

    private final MediaParamsPaneController controller;

    public OpenRealTimeDialog(ResourceBundle i18n) {
        controller = MediaParamsPaneController.newInstance();

        setTitle(i18n.getString("realtime.dialog.title"));
        setHeaderText(i18n.getString("realtime.dialog.header"));
        setContentText(i18n.getString("realtime.dialog.content"));

        getDialogPane().setContent(controller.getRoot());
        ButtonType ok = new ButtonType(i18n.getString("global.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType(i18n.getString("global.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(ok, cancel);

        setResultConverter(buttonType -> {
            MediaParams mediaParams = null;
            if (buttonType == ok) {
                String cameraId = controller.getCameraIdComboBox()
                        .getSelectionModel()
                        .getSelectedItem();
                String sn = controller.getSequenceNumberTextField().getText();
                if (cameraId != null && sn != null) {
                    mediaParams = new MediaParams(cameraId, Long.parseLong(sn));
                }
            }
            return mediaParams;
        });

    }

    /**
     * Loads the available platforms into the combobox
     */
    public void refresh() {
        controller.refresh();
    }


}
