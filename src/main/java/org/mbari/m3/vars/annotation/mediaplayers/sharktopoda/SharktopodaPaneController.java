package org.mbari.m3.vars.annotation.mediaplayers.sharktopoda;


import java.util.ResourceBundle;
import java.util.function.UnaryOperator;
import java.util.prefs.Preferences;

import com.typesafe.config.Config;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.messages.ShowNonfatalErrorAlert;
import org.mbari.m3.vars.annotation.ui.prefs.IPrefs;
import org.mbari.m3.vars.annotation.util.FXMLUtil;

/**
 * @author Brian Schlining
 * @since 2017-08-08T15:21:00
 */
public class SharktopodaPaneController implements IPrefs {

    @FXML
    private TextField controlPortTextField;

    @FXML
    private TextField framegrabPortTextField;

    @FXML
    private GridPane root;

    private Preferences prefs = Preferences.userNodeForPackage(getClass());
    public static final String CONTROL_PORT_KEY = "sharktopoda-control-port";
    public static final String FRAMEGRAB_PORT_KEY = "sharktopoda-framegrab-port";

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
        controlPortTextField.setTextFormatter(textFormatter1);
        framegrabPortTextField.setTextFormatter(textFormatter2);

        load();
    }

    /**
     *
     * @return Pair with Sharkopoda control port and Framecapture port
     */
    public static Pair<Integer, Integer> getPortNumbers() {
        Preferences prefs = Preferences.userNodeForPackage(SharktopodaPaneController.class);
        int dSharkPort = Initializer.CONFIG.getInt("sharktopoda.defaults.control.port");
        int dFgPort = Initializer.CONFIG.getInt("sharktopoda.defaults.framegrab.port");

        try {
            int cport = prefs.getInt(CONTROL_PORT_KEY, dSharkPort);
            int fport = prefs.getInt(FRAMEGRAB_PORT_KEY, dFgPort);
            return new Pair<>(cport, fport);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static SharktopodaPaneController newInstance() {
        return FXMLUtil.newInstance(SharktopodaPaneController.class,
                "/fxml/SharktopodaPane.fxml");
    }

    public GridPane getRoot() {
        return root;
    }

    @Override
    public void load() {
        int dSharkPort = Initializer.CONFIG.getInt("sharktopoda.defaults.control.port");
        int dFgPort = Initializer.CONFIG.getInt("sharktopoda.defaults.framegrab.port");

        int sharkPort = prefs.getInt(CONTROL_PORT_KEY, dSharkPort);
        int fgPort = prefs.getInt(FRAMEGRAB_PORT_KEY, dFgPort);
        controlPortTextField.setText(sharkPort + "");
        framegrabPortTextField.setText(fgPort + "");
    }

    @Override
    public void save() {
        Config config = Initializer.CONFIG;
        ResourceBundle i18n = Initializer.getToolBox().getI18nBundle();
        int sharkPort = config.getInt("sharktopoda.defaults.control.port");
        int fgPort = config.getInt("sharktopoda.defaults.framegrab.port");

        try {
            sharkPort = Integer.parseInt(controlPortTextField.getText());
            fgPort = Integer.parseInt(framegrabPortTextField.getText());
        }
        catch (Exception e) {
            Initializer.getToolBox()
                    .getEventBus()
                    .send(new ShowNonfatalErrorAlert(i18n.getString("mediaplayer.sharktopoda.error.title"),
                            i18n.getString("mediaplayer.sharktopoda.error.header"),
                            i18n.getString("mediaplayer.sharktopoda.error.content"),
                            e));
        }
        prefs.putInt(CONTROL_PORT_KEY, sharkPort);
        prefs.putInt(FRAMEGRAB_PORT_KEY, fgPort);
    }
}
