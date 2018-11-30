package org.mbari.m3.vars.annotation.ui.shared;

import io.reactivex.rxjavafx.observables.JavaFxObservable;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Details;

import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2018-11-30T14:53:00
 */
public class DetailsDialog extends Dialog<Details> {

    private final SearchableDetailEditorPaneController controller;

    public DetailsDialog(UIToolBox toolBox) {


        controller = SearchableDetailEditorPaneController.newInstance(toolBox);

        ResourceBundle i18n = toolBox.getI18nBundle();
        ButtonType ok = new ButtonType(i18n.getString("global.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType(i18n.getString("global.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        DialogPane dialogPane = getDialogPane();
        dialogPane.getButtonTypes().addAll(ok, cancel);
        dialogPane.setContent(controller.getRoot());
        dialogPane.getStylesheets().addAll(toolBox.getStylesheets());
        dialogPane.setPrefWidth(700);


//        JavaFxObservable.valuesOf(dialogPane.widthProperty())
//                .subscribe(n -> controller.getRoot().setPrefWidth(n.doubleValue() - 10D));

        setResultConverter(buttonType -> {
            if (buttonType == ok) {
                return controller.getCustomAssociation().orElse(null);
            }
            else {
                return null;
            }
        });

    }

    public SearchableDetailEditorPaneController getController() {
        return controller;
    }
}
