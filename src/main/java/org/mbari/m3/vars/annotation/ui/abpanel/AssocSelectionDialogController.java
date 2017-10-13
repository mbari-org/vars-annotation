package org.mbari.m3.vars.annotation.ui.abpanel;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.ConceptAssociationTemplate;

import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-10-12T10:36:00
 */
public class AssocSelectionDialogController {

    private UIToolBox toolBox;
    private Dialog<NamedAssociation> dialog;

    @FXML
    private GridPane root;

    @FXML
    private Label assocLabel;

    @FXML
    private TextField searchTextField;

    @FXML
    private ComboBox<ConceptAssociationTemplate> assocComboBox;

    @FXML
    private TextField nicknameTextField;

    @FXML
    void initialize() {
        toolBox = Initializer.getToolBox();
    }

    public Dialog<NamedAssociation> getDialog() {
        if (dialog == null) {

            dialog = new Dialog<>();
            ResourceBundle i18n = toolBox.getI18nBundle();
            dialog.setTitle(i18n.getString("abpane.dialog.title"));
            dialog.setHeaderText(i18n.getString("abpane.dialog.header"));
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().setContent(root);
            dialog.setResultConverter(btnType -> {
                NamedAssociation na = null;
                if (btnType == ButtonType.OK) {
                    ConceptAssociationTemplate item = assocComboBox.getSelectionModel().getSelectedItem();
                    String name = nicknameTextField.getText();
                    if (item != null && name != null && name.isEmpty()) {
                        na = new NamedAssociation(item, name);
                    }
                }
                return na;
            });

            // --- Disable OK button when values are invalid
            Node node = dialog.getDialogPane().lookupButton(ButtonType.OK);
            node.setDisable(true);
            Runnable r = () -> {
                boolean isNameEmpty = nicknameTextField.getText().trim().isEmpty();
                boolean isAssocEmpty = assocComboBox.getSelectionModel().isEmpty();
                boolean disable = isAssocEmpty || isNameEmpty;
                node.setDisable(disable);
            };

            assocComboBox.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((obs, oldv, newv) -> r.run());

            nicknameTextField.textProperty()
                    .addListener((obs, oldv, newv) -> r.run());
        }
        return dialog;
    }



    public static AssocSelectionDialogController newInstance(UIToolBox toolBox) {
        final ResourceBundle bundle = toolBox.getI18nBundle();
        FXMLLoader loader = new FXMLLoader(AssocSelectionDialogController.class
                .getResource("/fxml/AssocSelectionPane.fxml"), bundle);
        try {
            loader.load();
            AssocSelectionDialogController controller = loader.getController();
            return controller;
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to load AssocSelectionPane from FXML", e);
        }
    }

}


