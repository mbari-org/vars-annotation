package org.mbari.vars.ui.javafx.prefs;

import com.jfoenix.controls.JFXTextField;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.mbari.vars.ui.UIToolBox;

public class RazielPaneController {

    private Pane root;
    private Pane statusPane;

    private final UIToolBox toolBox;


    public RazielPaneController(UIToolBox toolBox) {
        this.toolBox = toolBox;
    }

    public Pane getRoot() {
        if (root == null) {
            var i18n = toolBox.getI18nBundle();

            var label = new Label(i18n.getString("raziel.label"));

            var textField = new JFXTextField();
            textField.setPromptText(i18n.getString("raziel.prompt"));
            textField.textProperty().addListener((obs, oldvalue, newvalue) -> {

            });
            root = new VBox(label, textField);
        }
        return root;
    }


}
