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

import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.prefs.Preferences;

/**
 * @author Brian Schlining
 * @since 2020-03-03T15:22:00
 */
public class LocalizationPrefsPaneController implements IPrefs {

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

    private Preferences prefs = Preferences.userNodeForPackage(getClass());

    private EventBus eventBus;
    private UIToolBox toolBox;
    private static final String IN_PORT_KEY = "incoming-port";
    private static final String IN_TOPIC_KEY = "incoming-topic";
    private static final String OUT_PORT_KEY = "outgoing-port";
    private static final String OUT_TOPIC_KEY = "outgoing-topic";

    /**
     * Internal class to load and hold default config values.
     */
    private static class DefaultConfig {
        int incomingPort;
        String incomingTopic;
        int outgoingPort;
        String outgoingTopic;

        static DefaultConfig fromAppConfig(AppConfig appConfig) {
            DefaultConfig c = new DefaultConfig();
            c.incomingPort = appConfig.getLocalizationDefaultsIncomingPort();
            c.incomingTopic = appConfig.getLocalizationDefaultsIncomingTopic();
            c.outgoingPort = appConfig.getLocalizationDefaultsOutgoingPort();
            c.outgoingTopic = appConfig.getLocalizationDefaultsOutgoingTopic();
            return c;
        }

        void save(Preferences prefs) {
            prefs.putInt(IN_PORT_KEY, incomingPort);
            prefs.put(IN_TOPIC_KEY, incomingTopic);
            prefs.putInt(OUT_PORT_KEY, outgoingPort);
            prefs.put(OUT_TOPIC_KEY, outgoingTopic);
        }

        void load(Preferences prefs) {
            incomingPort = prefs.getInt(IN_PORT_KEY, incomingPort);
            incomingTopic = prefs.get(IN_TOPIC_KEY, incomingTopic);
            outgoingPort = prefs.getInt(OUT_PORT_KEY, outgoingPort);
            outgoingTopic = prefs.get(OUT_TOPIC_KEY, outgoingTopic);
        }

        void updateConfigFromUI(LocalizationPrefsPaneController controller) {
            incomingPort = Integer.parseInt(controller.incomingPortTextField.getText());
            incomingTopic = controller.incomingTopicTextField.getText();
            outgoingPort = Integer.parseInt(controller.outgoingPortTextField.getText());
            outgoingTopic = controller.outgoingTopicTextField.getText();
        }

        void updateUIFromConfig(LocalizationPrefsPaneController controller) {
            controller.incomingPortTextField.setText(incomingPort + "");
            controller.incomingTopicTextField.setText(incomingTopic);
            controller.outgoingPortTextField.setText(outgoingPort + "");
            controller.outgoingTopicTextField.setText(outgoingTopic);
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
        DefaultConfig c = DefaultConfig.fromAppConfig(appConfig);
        c.load(prefs);
        c.updateUIFromConfig(this);
    }

    @Override
    public void save() {
        AppConfig appConfig = toolBox.getAppConfig();
        DefaultConfig c = DefaultConfig.fromAppConfig(appConfig);
        ResourceBundle i18n = toolBox.getI18nBundle();
        try {
            c.updateConfigFromUI(this);
        }
        catch (Exception e) {
            // TODO send ShowFatalErrorAlert on eventbus
        }
        c.save(prefs);

    }

    public static LocalizationPrefsPaneController newInstance(UIToolBox toolBox) {
        final ResourceBundle i18n = toolBox.getI18nBundle();
        LocalizationPrefsPaneController controller = FXMLUtils.newInstance(LocalizationPrefsPaneController.class,
                "/fxml/LocalizationPrefsPane.fxml", i18n);
        controller.eventBus = toolBox.getEventBus();
        controller.toolBox = toolBox;
        return controller;
    }
}
