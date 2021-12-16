package org.mbari.vars.ui.javafx.raziel;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.mbari.vars.services.impl.raziel.RazielConfigurationService;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.domain.RazielConnectionParams;
import org.mbari.vars.ui.mediaplayers.SettingsPane;
import org.mbari.vars.ui.messages.ReloadServicesMsg;
import org.mbari.vars.ui.util.FXMLUtils;
import org.mbari.vars.ui.util.JFXUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
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
    private JFXButton testButton;

    @FXML
    private TextField urlTextfield;

    @FXML
    private TextField usernameTextfield;

    @FXML
    private Label msgLabel;

    private String name;
    private final Logger log = LoggerFactory.getLogger(getClass());

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

    private Optional<RazielConnectionParams> parseRazielConnectionParams() {
        try {
            var urlText = urlTextfield.getText();
            var userText = usernameTextfield.getText();
            var pwdText = passwordTextfield.getText();
            var ok = urlText != null && userText != null && pwdText != null &&
                    urlText.length() > 0 && userText.length() > 0 && pwdText.length() > 0;
            if (ok) {
                if (!urlText.startsWith("http://")) {
                    urlText = "http://" + urlText;
                }
                URL url = new URL(urlText);
                var rcp = new RazielConnectionParams(url, userText, pwdText);
                return Optional.of(rcp);
            }
        }
        catch (Exception e) {
            log.atDebug().setCause(e).log(() -> "Failed to parse connection params from the UI fields");
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
        var service = new RazielConfigurationService();
        var rcp = opt.get();
        service.checkStatus(rcp.url(), rcp.username(), rcp.password())
                        .handle((statuses, ex) -> {
                            if (ex != null) {
                                var s = resources.getString("raziel.pane.msg.authfailed");
                                Platform.runLater(() -> msgLabel.setText(s));
                                log.atDebug()
                                        .setCause(ex)
                                        .log("An exception occurred while running text against Raziel at" + rcp.url());
                            }
                            else {
                                var sortedStatuses = statuses.stream()
                                        .sorted(Comparator.comparing(es -> es.getEndpointConfig().getName()))
                                        .collect(Collectors.toList());
                                var panes = EndpointStatusPaneController.from(sortedStatuses)
                                        .stream()
                                        .map(EndpointStatusPaneController::getRoot)
                                        .collect(Collectors.toList());
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
        Initializer.loadConnectionParams()
                .ifPresent(rcp -> {
                    urlTextfield.setText(rcp.url().toExternalForm());
                    usernameTextfield.setText(rcp.username());
                    passwordTextfield.setText(rcp.password());
                    checkEnable();
                });
    }

    @Override
    public void save() {
        parseRazielConnectionParams().ifPresent(rcp -> {
            var path = Initializer.getConnectionParamsPath();
            var aes = Initializer.getToolBox().getAes();
            try {
                rcp.write(path, aes);
                var toolbox = Initializer.getToolBox();
                var services = Initializer.loadServices();

                // --- Update services and trigger reload of service dependant data.
                log.debug("Updating services using configuration from " + rcp.url());
                toolbox.setServices(services);
                Initializer.getToolBox().getEventBus().send(new ReloadServicesMsg());
            } catch (IOException e) {
                Platform.runLater(() -> msgLabel.setText("Failed to save connection params"));
                log.atWarn()
                        .setCause(e)
                        .log("Failed to save raziel connection parameters");
            }
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
