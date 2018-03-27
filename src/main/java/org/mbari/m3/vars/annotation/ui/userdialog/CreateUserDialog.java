package org.mbari.m3.vars.annotation.ui.userdialog;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.User;

import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2018-02-06T10:38:00
 */
public class CreateUserDialog extends Dialog<User> {

    private UserEditorPaneController controller;

    public CreateUserDialog(UIToolBox toolBox) {
        I18N i18n = new I18N(toolBox.getI18nBundle());
        setTitle(i18n.title);
        setHeaderText(i18n.header);
        setContentText(i18n.content);
        controller = UserEditorPaneController.newInstance();
        getDialogPane().setContent(controller.getRoot());
        ButtonType ok = new ButtonType(i18n.ok, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType(i18n.cancel, ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(ok, cancel);
        getDialogPane().getStylesheets().addAll(toolBox.getStylesheets());

        setResultConverter( buttonType -> {
            User user = null;
            if (buttonType == ok) {
                Optional<User> opt = controller.getUser();

                // Create opt in background
                if (opt.isPresent()) {
                    controller.createUser(toolBox.getServices().getUserService(),
                            toolBox.getEventBus());
                    user = opt.get();
                }
            }
            return user;
        });
    }

    // parse hte i18n resources needed for this class
    class I18N {
        final String title;
        final String header;
        final String content;
        final String ok;
        final String cancel;

        I18N(ResourceBundle i18n) {
            title = i18n.getString("userdialog.title");
            header = i18n.getString("userdialog.header");
            content = i18n.getString("userdialog.content");
            ok = i18n.getString("global.ok");
            cancel = i18n.getString("global.cancel");
        }

    }

}
