package org.mbari.vars.annotation.ui.javafx.buttons;


import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.text.Text;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.commands.CreateAssociationsCmd;
import org.mbari.vars.annotation.ui.javafx.Icons;
import org.mbari.vars.annotation.ui.javafx.shared.ConceptSelectionDialogController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-09-13T09:21:00
 */
public class UponBC extends AbstractBC {

    private final String associationKey;
    private final String uponRoot;
    private ConceptSelectionDialogController dialogController;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public UponBC(Button button, UIToolBox toolBox) {
        super(button, toolBox);
        this.associationKey = toolBox.getAppConfig().getRoot().app().annotation().uponLinkname();
        this.uponRoot = toolBox.getAppConfig().getRoot().app().annotation().uponRoot();
        dialogController = new ConceptSelectionDialogController(toolBox);
        dialogController.setConcept(this.uponRoot, false);
    }

    protected void init() {

        String tooltip = toolBox.getI18nBundle().getString("buttons.upon");
        Text icon = Icons.VERTICAL_ALIGN_BOTTOM.standardSize();
        Text icon2 = Icons.VERTICAL_ALIGN_BOTTOM.standardSize();
        initializeButton(tooltip, icon);

        ResourceBundle i18n = toolBox.getI18nBundle();
        Dialog<String> dialog = dialogController.getDialog();
        dialog.setTitle(i18n.getString("buttons.upon.dialog.title"));
        dialog.setHeaderText(i18n.getString("buttons.upon.dialog.header"));
        dialog.setContentText(i18n.getString("buttons.upon.dialog.content"));
        dialog.setGraphic(icon2);
        dialog.getDialogPane().getStylesheets().addAll(toolBox.getStylesheets());

    }

    protected void apply() {
        Dialog<String> dialog = dialogController.getDialog();
        dialogController.requestFocus();
        Optional<String> opt = dialog.showAndWait();
        opt.ifPresent(selectedItem -> {
            log.debug("Select upon substrate of " + selectedItem);
            Association association = new Association(associationKey,
                    selectedItem,
                    Association.VALUE_NIL);
            List<Annotation> selectedAnnotations = new ArrayList<>(toolBox.getData().getSelectedAnnotations());
            toolBox.getEventBus()
                    .send(new CreateAssociationsCmd(association, selectedAnnotations));
        });
    }


}
