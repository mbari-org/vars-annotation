package org.mbari.m3.vars.annotation.ui.shared;

import io.reactivex.rxjavafx.observables.JavaFxObservable;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
        Button okButton = (Button) dialogPane.lookupButton(ok);

        // When search textfield is focused disable defautl button or it
        // eats the enter keys strokes preventing search from working
        controller.getSearchTextField()
                .focusedProperty()
                .addListener((obj, oldv, newv) -> okButton.setDefaultButton(!newv));



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
