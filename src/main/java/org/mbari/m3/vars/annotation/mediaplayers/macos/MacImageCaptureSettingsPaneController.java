package org.mbari.m3.vars.annotation.mediaplayers.macos;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXRadioButton;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import com.typesafe.config.Config;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.util.FXMLUtils;

/**
 * @author Brian Schlining
 * @since 2018-04-24T16:50:00
 */
public class MacImageCaptureSettingsPaneController {

    public static final String DEVICE_KEY = "macos-imagecapture-device";
    public static final String CAPTURE_API_KEY ="macos-imagecapture-api";

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private GridPane root;

    @FXML
    private JFXRadioButton noneRadioButton;

    @FXML
    private JFXRadioButton avfRadioButton;

    @FXML
    private JFXRadioButton bmRadioButton;

    @FXML
    private JFXComboBox<?> deviceComboBox;

    @FXML
    void initialize() {

    }

    public static MacImageCaptureSettingsPaneController newInstance() {
        return FXMLUtils.newInstance(MacImageCaptureSettingsPaneController.class,
                "/fxml/MacImageCaptureSettingsPane.fxml");
    }
    

    public static String getSelectedDevice() {
        Preferences prefs = Preferences.userNodeForPackage(SettingsPaneImpl.class);
        return prefs.get(DEVICE_KEY, "");
    }

    public static String getSelectedCaptureApi() {
        Preferences prefs = Preferences.userNodeForPackage(SettingsPaneImpl.class);
        return prefs.get(CAPTURE_API_KEY, "");
    }
}
