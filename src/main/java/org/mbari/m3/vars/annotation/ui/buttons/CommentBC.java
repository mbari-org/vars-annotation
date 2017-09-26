package org.mbari.m3.vars.annotation.ui.buttons;

import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.CreateAssociationsCmd;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.User;

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

        dialog = new TextInputDialog();

        ResourceBundle i18n = toolBox.getI18nBundle();
        String tooltip = i18n.getString("buttons.comment");
        MaterialIconFactory iconFactory = MaterialIconFactory.get();
        Text icon = iconFactory.createIcon(MaterialIcon.INSERT_COMMENT, "30px");
        initializeButton(tooltip, icon);

        dialog.setTitle(i18n.getString("buttons.comment.dialog.title"));
        dialog.setHeaderText(i18n.getString("buttons.comment.dialog.header"));
        dialog.setContentText(i18n.getString("buttons.comment.dialog.content"));
        dialog.setGraphic(icon);
        dialog.getDialogPane().getStylesheets().addAll(toolBox.getStylesheets());

        toolBox.getEventBus()
                .toObserverable()
                .ofType(AnnotationsSelectedEvent.class)
                .subscribe(e -> {
                    User user = toolBox.getData().getUser();
                    boolean enabled = (user != null) && e.get().size() > 0;
                    button.setDisable(!enabled);
                });
    }

    protected void apply() {
        ObservableList<Annotation> annotations = toolBox.getData().getSelectedAnnotations();
        Optional<String> s = dialog.showAndWait();
        s.ifPresent(comment -> {
            Association a = new Association(commentLinkName, Association.VALUE_SELF, comment);
            toolBox.getEventBus()
                    .send(new CreateAssociationsCmd(a, annotations));
        });
        dialog.getEditor().setText(null);
    }
}
