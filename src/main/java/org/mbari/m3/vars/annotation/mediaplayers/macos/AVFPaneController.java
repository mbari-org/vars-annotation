package org.mbari.m3.vars.annotation.mediaplayers.macos;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.mbari.m3.vars.annotation.mediaplayers.ships.MacImageCaptureService;
import org.mbari.m3.vars.annotation.services.ImageCaptureService;

import java.util.ResourceBundle;

/**
 * Settings pane for AVFoundation
 * @author Brian Schlining
 * @since 2017-08-11T09:25:00
 */
public class AVFPaneController {

    private VBox root;
    private ComboBox<String> comboBox;
    private final ResourceBundle i18n;

    public AVFPaneController(ResourceBundle i18n) {
        this.i18n = i18n;
    }

    public VBox getRoot() {
        if (root == null) {
            Label label = new Label(i18n.getString("mediaplayer.macos.label"));

            final ImageCaptureService ics = MacImageCaptureServiceRef.getImageCaptureService();
            ObservableList<String> devices = FXCollections.observableArrayList();
            if (ics instanceof AVFImageCaptureService) {
                AVFImageCaptureService avf = (AVFImageCaptureService) ics;
                devices.addAll(avf.listDevices());
            }
            ComboBox<String> comboBox = new ComboBox<>(devices);
            root = new VBox(label, comboBox)
        }
        return root;
    }

    private ComboBox<String> getComboBox() {
        if (comboBox == null) {
            final ImageCaptureService ics = MacImageCaptureServiceRef.getImageCaptureService();
            ObservableList<String> devices = FXCollections.observableArrayList();
            if (ics instanceof AVFImageCaptureService) {
                AVFImageCaptureService avf = (AVFImageCaptureService) ics;
                devices.addAll(avf.listDevices());
            }
            comboBox = new ComboBox<>(devices);

            // TODO add listener. WHen item is selected set it in local user preferences
        }
        return comboBox;
    }
}
