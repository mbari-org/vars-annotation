package org.mbari.vars.annotation.ui.mediaplayers.sharktopoda;


import java.time.Duration;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.prefs.Preferences;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import org.mbari.vars.annotation.AppConfig;
import org.mbari.vars.annotation.ui.Initializer;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.messages.ShowNonfatalErrorAlert;
import org.mbari.vars.annotation.ui.javafx.prefs.IPrefs;
import org.mbari.vars.annotation.ui.util.FXMLUtils;

/**
 * @author Brian Schlining
 * @since 2017-08-08T15:21:00
 */
public class SharktopodaSettingsPaneController implements IPrefs {

    @FXML
    private TextField controlPortTextField;

    @FXML
    private TextField framegrabPortTextField;

    @FXML
    private TextField timeJumpTextField;

    @FXML
    private TextField timeoutTextField;

    @FXML
    private RadioButton v1RadioButton;

    @FXML
    private RadioButton v2RadioButton;


    char a = 'b';

    @FXML
    private GridPane root;

    private Preferences prefs = Preferences.userNodeForPackage(getClass());
    public static final String CONTROL_PORT_KEY = "sharktopoda-control-port";
    public static final String FRAMEGRAB_PORT_KEY = "sharktopoda-framegrab-port";
    public static final String TIME_JUMP = "sharktopoda-time-jump";
    public static final String SHARKTOPODA_VERSION = "sharktopoda-version";
    public static final Integer DEFAULT_TIME_JUMP = 1000;
    public static final String FRAMECAPTURE_TIMEOUT = "sharktopoda-framecapture-timeout";
    public static final Integer DEFAULT_FRAMECAPTURE_TIMEOUT_SECONDS = 10;

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
        TextFormatter<String> textFormatter3 = new TextFormatter<>(filter);
        controlPortTextField.setTextFormatter(textFormatter1);
        framegrabPortTextField.setTextFormatter(textFormatter2);
        timeJumpTextField.setTextFormatter(textFormatter3);

        // Timeout in seconds. Validate the full resulting text so pasted values are checked too.
        // Must be a positive integer (an empty field is allowed while editing).
        UnaryOperator<TextFormatter.Change> timeoutFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[1-9][0-9]{0,3}") || newText.isEmpty()) {
                return change;
            }
            return null;
        };
        timeoutTextField.setTextFormatter(new TextFormatter<>(timeoutFilter));

        final ToggleGroup group = new ToggleGroup();
        v1RadioButton.setToggleGroup(group);
        v2RadioButton.setToggleGroup(group);

        v1RadioButton.setDisable(true);

        load();
    }

    /**
     *
     * @return Pair with Sharkopoda control port (key) and Framecapture port (value)
     */
    public static Pair<Integer, Integer> getPortNumbers() {
        Preferences prefs = Preferences.userNodeForPackage(SharktopodaSettingsPaneController.class);
        AppConfig appConfig = Initializer.getToolBox().getAppConfig();
        int dSharkPort = appConfig.getSharktopodaDefaultsControlPort();
        int dFgPort = appConfig.getSharktopodaDefaultsFramegrabPort();

        try {
            int cport = prefs.getInt(CONTROL_PORT_KEY, dSharkPort);
            int fport = prefs.getInt(FRAMEGRAB_PORT_KEY, dFgPort);
            return new Pair<>(cport, fport);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static Integer getTimeJump() {
        Preferences prefs = Preferences.userNodeForPackage(SharktopodaSettingsPaneController.class);
        try {
            return prefs.getInt(TIME_JUMP, DEFAULT_TIME_JUMP);
        }
        catch (Exception e) {
            return DEFAULT_TIME_JUMP;
        }
    }

    /**
     * @return The time to wait for Sharktopoda to complete a framecapture
     */
    public static Duration getFramecaptureTimeout() {
        Preferences prefs = Preferences.userNodeForPackage(SharktopodaSettingsPaneController.class);
        try {
            int seconds = prefs.getInt(FRAMECAPTURE_TIMEOUT, DEFAULT_FRAMECAPTURE_TIMEOUT_SECONDS);
            return Duration.ofSeconds(seconds > 0 ? seconds : DEFAULT_FRAMECAPTURE_TIMEOUT_SECONDS);
        }
        catch (Exception e) {
            return Duration.ofSeconds(DEFAULT_FRAMECAPTURE_TIMEOUT_SECONDS);
        }
    }

    public static Integer getSharktopodaVersion() {
        Preferences prefs = Preferences.userNodeForPackage(SharktopodaSettingsPaneController.class);
        try {
            return prefs.getInt(SHARKTOPODA_VERSION, 1);
        }
        catch (Exception e) {
            return 1;
        }
    }

    public static SharktopodaSettingsPaneController newInstance() {
        ResourceBundle i18n = Initializer.getToolBox().getI18nBundle();
        return FXMLUtils.newInstance(SharktopodaSettingsPaneController.class,
                "/fxml/SharktopodaSettingsPane.fxml",
                i18n);
    }

    public GridPane getRoot() {
        return root;
    }

    @Override
    public void load() {
        AppConfig appConfig = Initializer.getToolBox().getAppConfig();
        int dSharkPort = appConfig.getSharktopodaDefaultsControlPort();
        int dFgPort = appConfig.getSharktopodaDefaultsFramegrabPort();

        int sharkPort = prefs.getInt(CONTROL_PORT_KEY, dSharkPort);
        int fgPort = prefs.getInt(FRAMEGRAB_PORT_KEY, dFgPort);
        int timeJump = prefs.getInt(TIME_JUMP, DEFAULT_TIME_JUMP);
        controlPortTextField.setText(sharkPort + "");
        framegrabPortTextField.setText(fgPort + "");
        timeJumpTextField.setText(timeJump + "");
        timeoutTextField.setText(getFramecaptureTimeout().toSeconds() + "");

        int sharkVersion = prefs.getInt(SHARKTOPODA_VERSION, 1);
        if (sharkVersion == 2) {
            v2RadioButton.setSelected(true);
        } else {
            v1RadioButton.setSelected(true);
        }
    }

    @Override
    public void save() {

        UIToolBox toolBox = Initializer.getToolBox();
        AppConfig appConfig = toolBox.getAppConfig();
        int sharkPort = appConfig.getSharktopodaDefaultsControlPort();
        int fgPort = appConfig.getSharktopodaDefaultsFramegrabPort();
        int timeJump = toolBox.getData().getTimeJump();
        int timeout = (int) getFramecaptureTimeout().toSeconds();
        ResourceBundle i18n = toolBox.getI18nBundle();

        try {
            sharkPort = Integer.parseInt(controlPortTextField.getText());
            fgPort = Integer.parseInt(framegrabPortTextField.getText());
            timeJump = Integer.parseInt(timeJumpTextField.getText());
        }
        catch (Exception e) {
            toolBox.getEventBus()
                    .send(new ShowNonfatalErrorAlert(i18n.getString("mediaplayer.sharktopoda.error.title"),
                            i18n.getString("mediaplayer.sharktopoda.error.header"),
                            i18n.getString("mediaplayer.sharktopoda.error.content"),
                            e));
        }

        // Parsed separately so a bad timeout gets its own message and doesn't block the other values
        try {
            timeout = Integer.parseInt(timeoutTextField.getText());
        }
        catch (Exception e) {
            toolBox.getEventBus()
                    .send(new ShowNonfatalErrorAlert(i18n.getString("mediaplayer.sharktopoda.error.title"),
                            i18n.getString("mediaplayer.sharktopoda.error.header"),
                            i18n.getString("mediaplayer.sharktopoda.timeout.error.content"),
                            e));
        }
        prefs.putInt(CONTROL_PORT_KEY, sharkPort);
        prefs.putInt(FRAMEGRAB_PORT_KEY, fgPort);

        // Time jump is saved to prefs but also set in Data so it can be used immediatly
        prefs.putInt(TIME_JUMP, timeJump);
        toolBox.getData().setTimeJump(timeJump);

        // Read on each framecapture so changes take effect immediately
        if (timeout > 0) {
            prefs.putInt(FRAMECAPTURE_TIMEOUT, timeout);
        }

        var version = v2RadioButton.isSelected() ? 2 : 1;
        prefs.putInt(SHARKTOPODA_VERSION, version);


        // Raziel sends a ReloadServicesMsg which close the open media
//        Media media = toolBox.getData()
//                .getMedia();
//        if (media != null) {
//            toolBox.getEventBus()
//                    .send(new MediaChangedEvent(this, media));
//        }
    }
}
