package org.mbari.vars.annotation.ui.javafx.mlstage;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.ui.Initializer;
import org.mbari.vars.annotation.ui.mediaplayers.SettingsPane;
import org.mbari.vars.annotation.ui.util.FXMLUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class MLSettingsPaneController implements SettingsPane {

    @FXML
    private ResourceBundle resources;

    @FXML
    private GridPane root;

    @FXML
    private TextField urlTextField;

    private String name;
    private final Loggers log = new Loggers(getClass());
    private Preferences prefs = Preferences.userNodeForPackage(getClass());
    public static final String REMOTE_URL="ml-remote-url";

    @FXML
    void initialize() {
        name = resources.getString("ml.name");
    }

    @Override
    public void load() {
        var url = prefs.get(REMOTE_URL, "");
        urlTextField.setText(url);
    }

    @Override
    public void save() {
        try {
            var url = new URL(urlTextField.getText());
            prefs.put(REMOTE_URL, url.toExternalForm());
        }
        catch (MalformedURLException e) {
            log.atWarn().withCause(e).log("Failed to save ML endpoint setting to preferences");
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Pane getPane() {
        return root;
    }

    public static MLSettingsPaneController newInstance() {
        var i18n = Initializer.getToolBox().getI18nBundle();
        return FXMLUtils.newInstance(MLSettingsPaneController.class,
                "/fxml/MLSettingsPaneController.fxml",
                i18n);
    }

    public static Optional<String> getRemoteUrl() {
        var prefs = Preferences.userNodeForPackage(MLSettingsPaneController.class);
        return Optional.ofNullable(prefs.get(REMOTE_URL, null));
    }
}
