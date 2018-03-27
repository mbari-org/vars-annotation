package org.mbari.m3.vars.annotation.ui.userdialog;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.events.UserAddedEvent;
import org.mbari.m3.vars.annotation.messages.ShowInfoAlert;
import org.mbari.m3.vars.annotation.model.User;
import org.mbari.m3.vars.annotation.services.UserService;
import org.mbari.m3.vars.annotation.util.FXMLUtils;

/**
 * @author Brian Schlining
 * @since 2018-02-06T10:26:00
 */
public class UserEditorPaneController {

    @FXML
    private GridPane root;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField usernameTextfield;

    @FXML
    private PasswordField pwd1Textfield;

    @FXML
    private PasswordField pwd2Textfield;

    @FXML
    private TextField firstnameTextfield;

    @FXML
    private TextField lastnameTextfield;

    @FXML
    private TextField affiliationTextfield;

    @FXML
    private TextField emailTextfield;

    @FXML
    void initialize() {

    }

    public GridPane getRoot() {
        return root;
    }

    public Optional<User> getUser() {
        String username = usernameTextfield.getText();
        String password1 = pwd1Textfield.getText();
        String password2 = pwd2Textfield.getText();
        String firstName = firstnameTextfield.getText();
        String lastName = lastnameTextfield.getText();
        String affiliation = affiliationTextfield.getText();
        String email = emailTextfield.getText();

        boolean ok = username != null &&
                password1 != null &&
                password2 != null &&
                password1.equals(password2) &&
                firstName != null &&
                lastName != null &&
                email != null;

        if (ok) {
            return Optional.of(new User(username, password1, "User", firstName,
                    lastName, affiliation, email));
        }
        else {
            return Optional.empty();
        }
    }

    public void createUser(UserService service, EventBus eventBus) {
        Optional<User> opt = getUser();
        if (opt.isPresent()) {
            User user = opt.get();
            service.create(user)
                    .thenAccept(u -> eventBus.send(new UserAddedEvent(u)));
        }
        else {
            String title = resources.getString("userdialog.nouser.title");
            String header = resources.getString("userdialog.nouser.header");
            String content = resources.getString("userdialog.nouser.content");
            eventBus.send(new ShowInfoAlert(title, header, content));
        }


    }

    public static UserEditorPaneController newInstance() {
        return FXMLUtils.newInstance(UserEditorPaneController.class,
                "/fxml/UserEditorPane.fxml");
    }
}
