package org.mbari.m3.vars.annotation.ui.buttons;

import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import io.reactivex.Observable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.CreateAssociationsCmd;
import org.mbari.m3.vars.annotation.messages.ClearCacheMsg;
import org.mbari.m3.vars.annotation.model.*;
import org.mbari.m3.vars.annotation.ui.shared.FilteredComboBoxDecorator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-09-13T09:21:00
 */
public class UponBC extends AbstractBC{
    private ChoiceDialog<String> dialog;
    private final String associationKey;
    private final String uponRoot;

    public UponBC(Button button, UIToolBox toolBox) {
        super(button, toolBox);
        this.associationKey = toolBox.getConfig()
                .getString("app.annotation.upon.linkname");
        this.uponRoot  = toolBox.getConfig().getString("app.annotation.upon.root");
        init();
    }

    protected void init() {

        // TODO replace choice dialog with custom
        dialog = new ChoiceDialog<>();
        dialog.getDialogPane()
                .getChildrenUnmodifiable()
                .stream()
                .filter(node -> node instanceof ComboBox)
                .forEach(node -> new FilteredComboBoxDecorator<String>((ComboBox) node,
                        FilteredComboBoxDecorator.STARTSWITH));

        String tooltip = toolBox.getI18nBundle().getString("buttons.upon");
        MaterialIconFactory iconFactory = MaterialIconFactory.get();
        Text icon = iconFactory.createIcon(MaterialIcon.VERTICAL_ALIGN_BOTTOM, "30px");
        initializeButton(tooltip, icon);

        ResourceBundle i18n = toolBox.getI18nBundle();
        dialog.setTitle(i18n.getString("buttons.upon.dialog.title"));
        dialog.setHeaderText(i18n.getString("buttons.upon.dialog.header"));
        dialog.setContentText(i18n.getString("buttons.upon.dialog.content"));
        dialog.setGraphic(icon);
        dialog.getDialogPane().getStylesheets().addAll(toolBox.getStylesheets());

        Observable<Object> observable = toolBox.getEventBus().toObserverable();
        observable.ofType(ClearCacheMsg.class)
                .subscribe(m -> refresh());

        refresh();
    }

    protected void apply() {
        Optional<String> opt = dialog.showAndWait();
        opt.ifPresent(selectedItem -> {
            Association association = new Association(associationKey,
                    Association.VALUE_SELF,
                    selectedItem);
            List<Annotation> selectedAnnotations = new ArrayList<>(toolBox.getData().getSelectedAnnotations());
            toolBox.getEventBus()
                    .send(new CreateAssociationsCmd(association, selectedAnnotations));
        });
    }


    private void refresh() {

        toolBox.getServices()
                .getConceptService()
                .findRoot()
                .thenAccept(c -> {
                            List<String> concepts = c.flatten();
                            dialog.getItems().clear();
                            dialog.getItems().addAll(concepts);
                            dialog.setSelectedItem(uponRoot);
                        });


    }
}
