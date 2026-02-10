package org.mbari.vars.annotation.ui.javafx.buttons;


import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Text;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.commands.CreateAssociationsCmd;
import org.mbari.vars.annotation.ui.javafx.Icons;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;
import org.mbari.vars.annotation.ui.util.JFXUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-09-11T16:59:00
 */
public class CommentBC extends AbstractBC {

    private TextInputDialog dialog;
    private final String commentLinkName;

    public CommentBC(Button button, UIToolBox toolBox) {
        super(button, toolBox);
        this.commentLinkName = toolBox.getConfig().getString("app.annotation.sample.association.comment");
    }

    protected void init() {
        String tooltip = toolBox.getI18nBundle().getString("buttons.comment");
//        Text icon = iconFactory.createIcon(MaterialIcon.INSERT_COMMENT, "30px");
        Text icon = Icons.INSERT_COMMENT.standardSize();
        initializeButton(tooltip, icon);

    }

    private TextInputDialog getDialog() {
        if (dialog == null) {
            ResourceBundle i18n = toolBox.getI18nBundle();
//            Text icon = iconFactory.createIcon(MaterialIcon.INSERT_COMMENT, "30px");
            Text icon = Icons.INSERT_COMMENT.standardSize();
            dialog = new TextInputDialog();
            dialog.setTitle(i18n.getString("buttons.comment.dialog.title"));
            dialog.setHeaderText(i18n.getString("buttons.comment.dialog.header"));
            dialog.setContentText(i18n.getString("buttons.comment.dialog.content"));
            dialog.setGraphic(icon);
            dialog.getDialogPane().getStylesheets().addAll(toolBox.getStylesheets());
        }
        return dialog;
    }

    protected void apply() {
        List<Annotation> annotations = new ArrayList<>(toolBox.getData().getSelectedAnnotations());
        JFXUtilities.runOnFXThread(() -> {
            TextInputDialog dialog = getDialog();
            dialog.getEditor().requestFocus();
            Optional<String> s = dialog.showAndWait();
            s.ifPresent(comment -> {
                Association a = new Association(commentLinkName, Association.VALUE_SELF, comment);
                toolBox.getEventBus()
                        .send(new CreateAssociationsCmd(a, annotations));
            });
            dialog.getEditor().setText(null);
        });


    }
}
