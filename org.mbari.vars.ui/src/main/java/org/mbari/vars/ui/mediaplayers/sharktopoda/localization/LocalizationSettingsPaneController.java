package org.mbari.vars.ui.mediaplayers.sharktopoda.localization;


import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.mediaplayers.SettingsPane;
import org.mbari.vars.ui.util.FXMLUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

/**
 * UI for setting preferences for LocalizationSettings values.
 *
 * @author Brian Schlining
 * @since 2020-03-03T15:22:00
 */
public class LocalizationSettingsPaneController implements SettingsPane {

    @FXML
    private GridPane root;

    @FXML
    private TextField incomingPortTextField;

    @FXML
    private TextField outgoingPortTextField;

    @FXML
    private TextField incomingTopicTextField;

    @FXML
    private TextField outgoingTopicTextField;

    @FXML
    private CheckBox enabledCheckBox;

    private EventBus eventBus;
    private UIToolBox toolBox;


    private void updateUIFromSettings(LocalizationSettings settings) {
        incomingPortTextField.setText(settings.getIncomingPort() + "");
        incomingTopicTextField.setText(settings.getIncomingTopic());
        outgoingPortTextField.setText(settings.getOutgoingPort() + "");
        outgoingTopicTextField.setText(settings.getOutgoingTopic());
        enabledCheckBox.setSelected(settings.isEnabled());
    }

    private Optional<LocalizationSettings> settingsFromUI() {
        try {
            int incomingPort = Integer.parseInt(incomingPortTextField.getText());
            String incomingTopic = incomingTopicTextField.getText();
            int outgoingPort = Integer.parseInt(outgoingPortTextField.getText());
            String outgoingTopic = outgoingTopicTextField.getText();
            boolean enabled = enabledCheckBox.isSelected();
            LocalizationSettings settings = new LocalizationSettings(incomingPort,
                    incomingTopic, outgoingPort, outgoingTopic, enabled);
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

    }

    public GridPane getPane() {
        return root;
    }

    public String getName() {
        return toolBox.getI18nBundle().getString("localization.pane.title");
    }


    @Override
    public void load() {
        updateUIFromSettings(LocalizationPrefs.load(toolBox.getAppConfig()));
    }


    @Override
    public void save() {
        settingsFromUI().ifPresent(newSettings -> {
            LocalizationSettings oldSettings = LocalizationPrefs.load(toolBox.getAppConfig());
            if (!Objects.equals(newSettings, oldSettings)) {
                LocalizationPrefs.save(newSettings);

                // Raziel sends a ReloadServicesMsg which close the open media
                // Relaunch media player if one is opened
//                Media media = toolBox.getData().getMedia();
//                if (media != null) {
//                    toolBox.getEventBus()
//                            .send(new MediaChangedEvent(this, media));
//                }
            }
        });
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
