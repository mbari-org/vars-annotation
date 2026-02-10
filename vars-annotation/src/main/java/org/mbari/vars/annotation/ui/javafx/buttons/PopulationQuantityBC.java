package org.mbari.vars.annotation.ui.javafx.buttons;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Text;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.commands.CreateAssociationsCmd;
import org.mbari.vars.annotation.ui.javafx.Icons;
import org.mbari.vars.annotation.ui.util.JFXUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PopulationQuantityBC extends AbstractBC {

    private TextInputDialog dialog;
    private final String popQuantLinkName;

    public PopulationQuantityBC(Button button, UIToolBox toolBox) {
        super(button, toolBox);
        this.popQuantLinkName = toolBox.getConfig().getString("app.annotation.sample.association.population");
    }

    @Override
    protected void apply() {
        List<Annotation> annotations = new ArrayList<>(toolBox.getData().getSelectedAnnotations());
        TextInputDialog dialog = getDialog();
        JFXUtilities.runOnFXThread(() -> dialog.getEditor().requestFocus());
        Optional<String> s = dialog.showAndWait();
        s.ifPresent(population -> {
            Association a = new Association(popQuantLinkName, Association.VALUE_SELF, population);
            toolBox.getEventBus()
                    .send(new CreateAssociationsCmd(a, annotations));
        });
        getDialog().getEditor().setText(null);
    }

    @Override
    protected void init() {
        String tooltip = toolBox.getI18nBundle().getString("buttons.population");
        Text icon = Icons.GRAIN.standardSize();
        initializeButton(tooltip, icon);
    }

    private TextInputDialog getDialog() {
        if (dialog == null) {
            ResourceBundle i18n = toolBox.getI18nBundle();
//            Text icon = iconFactory.createIcon(MaterialIcon.INSERT_COMMENT, "30px");
            Text icon = Icons.INSERT_COMMENT.standardSize();
            dialog = new TextInputDialog();
            dialog.setTitle(i18n.getString("buttons.population.dialog.title"));
            dialog.setHeaderText(i18n.getString("buttons.population.dialog.header"));
            dialog.setContentText(i18n.getString("buttons.population.dialog.content"));
            dialog.setGraphic(icon);
            dialog.getDialogPane().getStylesheets().addAll(toolBox.getStylesheets());
            TextField textField = dialog.getEditor();
            // Only allow digits only
            textField.textProperty().addListener((obs, oldv, newv) -> {
                if (newv != null && !newv.matches("\\d*")) {
                    textField.setText(newv.replaceAll("[^\\d]", ""));
                }
            });
        }
        return dialog;
    }
}
