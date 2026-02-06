package org.mbari.vars.annotation.ui.mediaplayers.vcr;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import org.mbari.vars.annotation.ui.Initializer;
import org.mbari.vars.annotation.ui.util.FXMLUtils;
import org.mbari.vars.annotation.ui.util.JFXUtilities;
import org.mbari.vars.annotation.etc.jdk.Loggers;


/**
 * @author Brian Schlining
 * @since 2018-03-26T12:05:00
 */
public class MediaParamsPaneController {

    private final Loggers log = new Loggers(getClass());

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private GridPane root;

    @FXML
    private ComboBox<String> cameraIdComboBox;

    @FXML
    private TextField deploymentKeyTextField;

    @FXML
    private TextField tapeNumberTextField;

    @FXML
    private CheckBox hdCheckBox;

    @FXML
    private ComboBox<String> commportComboBox;

    @FXML
    void initialize() {

        // Allow only numbers to be entered in the text field
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getText();
            if (text.matches("[0-9]*")) {
                return change;
            }
            return null;
        };

        TextFormatter<String> textFormatter1 = new TextFormatter<>(filter);
        TextFormatter<String> textFormatter2 = new TextFormatter<>(filter);
        deploymentKeyTextField.setTextFormatter(textFormatter1);
        tapeNumberTextField.setTextFormatter(textFormatter2);

        commportComboBox.setEditable(false);
        ObservableList<String> serialPorts = FXCollections.observableArrayList(MediaControlsFactoryImpl.getSerialPorts());
        commportComboBox.setItems(serialPorts);
        MediaControlsFactoryImpl.getSelectedSerialPort()
                .ifPresent(sp -> commportComboBox.getSelectionModel().select(sp));
    }

    public void refresh() {
        Initializer.getToolBox()
                .getServices()
                .mediaService()
                .findAllCameraIds()
                .thenAccept(cameraIds -> {
                    JFXUtilities.runOnFXThread(() -> {
                        deploymentKeyTextField.setText(null);
                        tapeNumberTextField.setText(null);
                        hdCheckBox.setSelected(true);
                        final ObservableList<String> items = FXCollections.observableArrayList(cameraIds);
                        cameraIdComboBox.setItems(items);
                        updateUiWithDefaults();
                    });

                });
    }

    public Optional<MediaParams> getMediaParams() {
        Optional<MediaParams> params = Optional.empty();
        try {
            String rov = cameraIdComboBox.getSelectionModel().getSelectedItem();
            int diveNumber = Integer.parseInt(deploymentKeyTextField.getText());
            int tapeNumber = Integer.parseInt(tapeNumberTextField.getText());
            boolean isHd = hdCheckBox.isSelected();
            String serialPort = commportComboBox.getSelectionModel().getSelectedItem();
            if (rov != null && !rov.isEmpty() &&
                    serialPort != null && !serialPort.isEmpty()) {
                MediaParams mediaParams = new MediaParams(rov, diveNumber,
                        tapeNumber, isHd, serialPort);
                params = Optional.of(mediaParams);
            }

        }
        catch (Exception e) {
            log.atInfo().withCause(e).log("Failed to parse media params");
        }
        return params;
    }

    public static MediaParamsPaneController newInstance() {
        ResourceBundle i18n = Initializer.getToolBox().getI18nBundle();
        return FXMLUtils.newInstance(MediaParamsPaneController.class,
                "/fxml/VcrSettingsPane.fxml",
                i18n);
    }

    public GridPane getRoot() {
        return root;
    }

    private void updateUiWithDefaults() {
        try {
            String cameraid = Initializer.getToolBox()
                    .getConfig()
                    .getString("app.defaults.cameraid");
            cameraIdComboBox.getSelectionModel().select(cameraid);
        }
        catch (Exception e) {
            log.atInfo().log("No default cameraId was found in the configuration file." +
                    " (app.defaults.cameraid");
        }
    }
}
