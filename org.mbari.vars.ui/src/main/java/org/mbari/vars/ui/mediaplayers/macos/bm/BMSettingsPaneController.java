package org.mbari.vars.ui.mediaplayers.macos.bm;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.mediaplayers.SettingsPane;
import org.mbari.vars.ui.util.FXMLUtils;

import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

public class BMSettingsPaneController implements SettingsPane {

    private String name;

    @FXML
    private ResourceBundle resources;

    @FXML
    private GridPane root;

    @FXML
    private TextField hostTextfield;

    @FXML
    private TextField keyTextfield;

    @FXML
    private TextField portTextfield;

    @FXML
    private TextField timeoutTextfield;

    @FXML
    void initialize() {

        name = resources.getString("mediaplayer.macos.bm.name");

        // Only allow numbers to be typed into port text field
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getText();
            if (text.matches("[0-9]*")) {
                return change;
            }
            return null;
        };

        var textFormatter0 = new TextFormatter<>(filter);
        portTextfield.setTextFormatter(textFormatter0);

        var textFormatter1 = new TextFormatter<>(filter);
        timeoutTextfield.setTextFormatter(textFormatter1);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Pane getPane() {
        return root;
    }

    @Override
    public void load() {
        var host = Settings.getHost();
        var port = Settings.getPort();
        var apiKey = Settings.getApiKey();
        var timeout = Settings.getTimeout();
        configureSettings(host, port, apiKey, timeout);
    }

    @Override
    public void save() {
        Settings.saveSettings(hostTextfield.getText(),
                Integer.parseInt(portTextfield.getText()),
                keyTextfield.getText(),
                Integer.parseInt(timeoutTextfield.getText()));
    }

    public void configureSettings(String host, int port, String apiKey, int timeout) {
        hostTextfield.setText(host);
        portTextfield.setText("" + port);
        keyTextfield.setText(apiKey);
        timeoutTextfield.setText("" + timeout);
    }

    public static BMSettingsPaneController newInstance() {
        var i18n = Initializer.getToolBox().getI18nBundle();
        return FXMLUtils.newInstance(BMSettingsPaneController.class,
                "/fxml/BMSettingsPane.fxml",
                i18n);
    }
}
