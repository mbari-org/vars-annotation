package org.mbari.m3.vars.annotation.mediaplayers.macos;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXRadioButton;
import java.net.URL;
import java.util.Collection;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.mbari.m3.vars.annotation.mediaplayers.SettingsPane;
import org.mbari.m3.vars.annotation.util.FXMLUtils;
import org.mbari.m3.vars.annotation.util.JFXUtilities;

/**
 * @author Brian Schlining
 * @since 2018-04-24T16:50:00
 */
public class MacImageCaptureSettingsPaneController implements SettingsPane  {

    public static final String DEVICE_KEY = "macos-imagecapture-device";
    public static final String CAPTURE_API_KEY ="macos-imagecapture-api";
    private String name;


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
    private JFXComboBox<String> deviceComboBox;

    private final ToggleGroup toggleGroup = new ToggleGroup();

    @FXML
    void initialize() {

        name = resources.getString("mediaplayer.macos.name");

        noneRadioButton.setToggleGroup(toggleGroup);
        noneRadioButton.setUserData(CaptureApi.NONE);
        avfRadioButton.setToggleGroup(toggleGroup);
        avfRadioButton.setUserData(CaptureApi.AVFOUNDATION);
        bmRadioButton.setToggleGroup(toggleGroup);
        bmRadioButton.setUserData(CaptureApi.BLACKMAGIC_DESIGN);
        toggleGroup.selectedToggleProperty().addListener((obs, oldv, newv) -> {
            if (newv != null) {
                CaptureApi captureApi = (CaptureApi) newv.getUserData();
                setCaptureDevice(captureApi, null);
            }
        });
    }


    private void setCaptureDevice(CaptureApi captureApi, String deviceName) {
        Collection<String> devices = captureApi.getImageCaptureService().listDevices();
        Runnable r = () -> {
            toggleGroup.getToggles()
                    .stream()
                    .filter(toggle -> toggle.getUserData() == captureApi)
                    .forEach(toggleGroup::selectToggle);
            deviceComboBox.setItems(FXCollections.observableArrayList(devices));
            if (deviceName != null && devices.contains(deviceName)) {
                deviceComboBox.getSelectionModel().select(deviceName);
            }
            else {
                deviceComboBox.getSelectionModel().select(0);
            }
        };
        JFXUtilities.runOnFXThread(r);
    }


    public static MacImageCaptureSettingsPaneController newInstance() {
        return FXMLUtils.newInstance(MacImageCaptureSettingsPaneController.class,
                "/fxml/MacImageCaptureSettingsPane.fxml");
    }

    public void load() {
        String captureApi = CaptureApiSettings.getSelectedCaptureApi();
        String deviceName = CaptureApiSettings.getSelectedDevice();
        CaptureApi api = CaptureApi.findByName(captureApi);
        setCaptureDevice(api, deviceName);
    }

    public void save() {
        Toggle toggle = toggleGroup.getSelectedToggle();
        CaptureApi captureApi = CaptureApi.NONE;
        if (toggle != null) {
            captureApi = (CaptureApi) toggle.getUserData();
        }
        String deviceName = "";
        if (!deviceComboBox.getSelectionModel().isEmpty()) {
            deviceName = deviceComboBox.getSelectionModel().getSelectedItem();
        }
        CaptureApiSettings.setCaptureDevice(captureApi, deviceName);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Pane getPane() {
        return root;
    }
}
