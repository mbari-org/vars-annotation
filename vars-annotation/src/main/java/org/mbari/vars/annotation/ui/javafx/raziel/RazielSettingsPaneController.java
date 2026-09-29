package org.mbari.vars.annotation.ui.javafx.raziel;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.services.raziel.Raziel;
import org.mbari.vars.annotation.ui.Initializer;
import org.mbari.vars.annotation.ui.mediaplayers.SettingsPane;
import org.mbari.vars.annotation.ui.messages.ReloadServicesMsg;
import org.mbari.vars.annotation.ui.util.FXMLUtils;
import org.mbari.vars.annotation.ui.util.JFXUtilities;

import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.util.Comparator;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class RazielSettingsPaneController implements SettingsPane {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private VBox endpointStatusPane;

    @FXML
    private PasswordField passwordTextfield;

    @FXML
    private GridPane root;

    @FXML
    private Button testButton;

    @FXML
    private TextField urlTextfield;

    @FXML
    private TextField usernameTextfield;

    @FXML
    private Label msgLabel;

    private String name;
    private final Loggers log = new Loggers(getClass());

    @FXML
    void initialize() {

        name = resources.getString("raziel.name");

        // Enable/disable test button
        usernameTextfield.textProperty().addListener((obs, oldv, newv) -> checkEnable());
        urlTextfield.textProperty().addListener((obs, oldv, newv) -> checkEnable());
        passwordTextfield.textProperty().addListener((obs, oldv, newv) -> checkEnable());

        testButton.setOnAction(event -> test());

        JFXUtilities.attractAttention(testButton);
    }

    private Optional<Raziel.ConnectionParams> parseRazielConnectionParams() {
        try {
            var urlText = urlTextfield.getText();
            var userText = usernameTextfield.getText();
            var pwdText = passwordTextfield.getText();
            var ok = urlText != null && userText != null && pwdText != null &&
                    urlText.length() > 0 && userText.length() > 0 && pwdText.length() > 0;
            if (ok) {

                // if (!urlText.startsWith("http://")) {
                //     urlText = "http://" + urlText;
                // }

                URL url = URI.create(urlText).toURL();
                var connectionParams = new Raziel.ConnectionParams(url, userText, pwdText);
                return Optional.of(connectionParams);
            }
        }
        catch (Exception e) {
            log.atDebug().withCause(e).log(() -> "Failed to parse connection params from the UI fields");
            // Do nothing
        }
        return Optional.empty();
    }

    private void checkEnable() {
        var opt = parseRazielConnectionParams();
        testButton.setDisable(opt.isEmpty());
    }

    private void test() {
        endpointStatusPane.getChildren().clear();
        msgLabel.setText(resources.getString("raziel.pane.msg.starting"));
        var opt = parseRazielConnectionParams();
        if (opt.isEmpty()) {
            var msg = resources.getString("raziel.pane.msg.invalidparams");
            log.atDebug().log("Invalid raziel connection params");
            Platform.runLater(() -> msgLabel.setText(msg));
            return;
        }
        var rcp = opt.get();
        Raziel.checkStatus(rcp.url(), rcp.username(), rcp.password())
                        .handle((statuses, ex) -> {
                            if (ex != null) {
                                var s = resources.getString("raziel.pane.msg.authfailed");
                                Platform.runLater(() -> msgLabel.setText(s));
                                log.atDebug()
                                        .withCause(ex)
                                        .log("An exception occurred while running text against Raziel at" + rcp.url());
                            }
                            else {
                                var sortedStatuses = statuses.stream()
                                        .sorted(Comparator.comparing(es -> es.endpointConfig().name()))
                                        .toList();
                                var panes = EndpointStatusPaneController.from(sortedStatuses)
                                        .stream()
                                        .map(EndpointStatusPaneController::getRoot)
                                        .toList();
                                Platform.runLater(() -> {
                                    msgLabel.setText(null);
                                    endpointStatusPane.getChildren().addAll(panes);
                                });

                            }
                            return null;
                        });

    }


    @Override
    public void load() {
        // The saved file is encoded by Raziel and can't be decoded here, so we can only show the url.
        // The user re-enters their credentials if they want to change them.
        Raziel.LoginFile.load()
                .ifPresent(loginFile -> {
                    urlTextfield.setText(loginFile.url().toExternalForm());
                    checkEnable();
                });
    }

    @Override
    public void save() {
        parseRazielConnectionParams().ifPresent(rcp -> {
            // Raziel checks the credentials and builds the login file. Only if that works do we
            // replace raziel.txt, so bad credentials never clobber a working file.
            Raziel.encode(rcp)
                    .whenComplete((loginFile, ex) -> {
                        if (ex != null) {
                            Platform.runLater(() -> msgLabel.setText(resources.getString("raziel.pane.msg.authfailed")));
                            log.atWarn()
                                    .withCause(ex)
                                    .log("Failed to get a login file from Raziel at " + rcp.url());
                            return;
                        }
                        try {
                            loginFile.write(Raziel.LoginFile.path());
                        }
                        catch (IOException e) {
                            Platform.runLater(() -> msgLabel.setText("Failed to save connection params"));
                            log.atWarn()
                                    .withCause(e)
                                    .log("Failed to save the Raziel login file");
                            return;
                        }
                        Platform.runLater(() -> {
                            var toolbox = Initializer.getToolBox();
                            var services = Initializer.loadServices();

                            // --- Update services and trigger reload of service dependant data.
                            log.debug("Updating services using configuration from " + rcp.url());
                            toolbox.setServices(services);
                            toolbox.getEventBus().send(new ReloadServicesMsg());
                        });
                    });
        });
        endpointStatusPane.getChildren().clear();

    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Pane getPane() {
        return root;
    }

    public static RazielSettingsPaneController newInstance() {
        var i18n = Initializer.getToolBox().getI18nBundle();
        return FXMLUtils.newInstance(RazielSettingsPaneController.class,
          "/fxml/RazielSettingsPane.fxml",
                i18n);

    }
}
