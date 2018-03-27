package org.mbari.m3.vars.annotation.mediaplayers.vcr;


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
    }
}
