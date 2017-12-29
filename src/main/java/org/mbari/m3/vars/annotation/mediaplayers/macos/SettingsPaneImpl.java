package org.mbari.m3.vars.annotation.mediaplayers.macos;

import com.jfoenix.controls.JFXComboBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.mediaplayers.SettingsPane;

import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * @author Brian Schlining
 * @since 2017-12-29T09:49:00
 */
public class SettingsPaneImpl implements SettingsPane {

    private HBox pane;
    private Label label = new Label();
    private ComboBox<String> comboBox = new JFXComboBox<>();
    private final AVFImageCaptureService imageCaptureService;
    private Preferences prefs = Preferences.userNodeForPackage(getClass());
    public static final String DEVICE_KEY = "macos-imagecapture-device";
    private final UIToolBox toolBox;
    private final String noDevice;
    private final String name;

    public SettingsPaneImpl(UIToolBox toolBox, AVFImageCaptureService imageCaptureService) {
        this.imageCaptureService = imageCaptureService;
        this.toolBox = toolBox;
        final ResourceBundle i18n = toolBox.getI18nBundle();
        noDevice = i18n.getString("mediaplayer.macos.default.device");
        name = i18n.getString("mediaplayer.macos.name");
        label.setText(i18n.getString("mediaplayer.macos.label"));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Pane getPane() {
        if (pane == null) {
            pane = new HBox(label, comboBox);
        }
        return pane;
    }

    @Override
    public void load() {
        final ObservableList<String> devices =
                FXCollections.observableArrayList(imageCaptureService.listDevices());
        comboBox.setItems(devices);
        final String selected = prefs.get(DEVICE_KEY, "");
        final ObservableList<String> items = comboBox.getItems();
        if (!items.contains(selected)) {
            items.add(noDevice);
            comboBox.getSelectionModel().select(noDevice);
        }
        else {
            comboBox.getSelectionModel().select(selected);
        }
    }

    @Override
    public void save() {
        if (!comboBox.getSelectionModel().isEmpty()) {
            String selected = comboBox.getSelectionModel().getSelectedItem();
            prefs.put(DEVICE_KEY, selected);
        }
    }
}
