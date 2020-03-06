package org.mbari.vars.ui.javafx.localization;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.TextFormatter;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.ui.AppConfig;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.javafx.prefs.IPrefs;
import org.mbari.vars.ui.util.FXMLUtils;

import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.prefs.Preferences;

/**
 * @author Brian Schlining
 * @since 2020-03-03T15:22:00
 */
public class LocalizationSettingsPaneController implements IPrefs {

    @FXML
    private JFXTextField incomingPortTextField;

    @FXML
    private JFXTextField outgoingPortTextField;

    @FXML
    private JFXTextField incomingTopicTextField;

    @FXML
    private JFXTextField outgoingTopicTextField;

    @FXML
    private JFXCheckBox enabledCheckBox;

    private EventBus eventBus;
    private UIToolBox toolBox;


    private void updateUIFromSettings(LocalizationSettings settings) {
        incomingPortTextField.setText(settings.getIncomingPort() + "");
        incomingTopicTextField.setText(settings.getIncomingTopic());
        outgoingPortTextField.setText(settings.getOutgoingPort() + "");
        outgoingTopicTextField.setText(settings.getOutgoingTopic());
    }

    private Optional<LocalizationSettings> settingsFromUI() {
        try {
            int incomingPort = Integer.parseInt(incomingPortTextField.getText());
            String incomingTopic = incomingTopicTextField.getText();
            int outgoingPort = Integer.parseInt(outgoingPortTextField.getText());
            String outgoingTopic = outgoingTopicTextField.getText();
            LocalizationSettings settings = new LocalizationSettings(incomingPort, incomingTopic, outgoingPort, outgoingTopic);
            return Optional.of(settings);
        }
        catch (Exception e) {
            return Optional.empty();
        }
    }

    @FXML
    void initialize() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getText();
            if (text.matches("[0-9]*")) {
                return change;
            }
            return null;
        };

        TextFormatter<String> textFormatter1 = new TextFormatter<>(filter);
        TextFormatter<String> textFormatter2 = new TextFormatter<>(filter);

        incomingPortTextField.setTextFormatter(textFormatter1);
        outgoingPortTextField.setTextFormatter(textFormatter2);

        load();

    }

    @Override
    public void load() {
        AppConfig appConfig = toolBox.getAppConfig();
        LocalizationSettings defaultSettings = new LocalizationSettings(appConfig);
        LocalizationSettings settings = LocalizationPrefs.load(defaultSettings);
        updateUIFromSettings(settings);
    }


    @Override
    public void save() {
        settingsFromUI().ifPresent(LocalizationPrefs::save);
    }

    public static LocalizationSettingsPaneController newInstance(UIToolBox toolBox) {
        final ResourceBundle i18n = toolBox.getI18nBundle();
        LocalizationSettingsPaneController controller = FXMLUtils.newInstance(LocalizationSettingsPaneController.class,
                "/fxml/LocalizationSettingsPane.fxml", i18n);
        controller.eventBus = toolBox.getEventBus();
        controller.toolBox = toolBox;
        return controller;
    }

}
