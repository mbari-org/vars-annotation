package org.mbari.vars.ui.javafx.abpanel;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.messages.ReloadServicesMsg;
import org.mbari.vars.services.model.ConceptAssociationTemplate;
import org.mbari.vars.core.util.ListUtils;

import java.util.List;
import java.util.Optional;
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
        toolBox.getEventBus()
                .toObserverable()
                .ofType(ReloadServicesMsg.class)
                .subscribe(m -> updateControls());

        // Trigger search when enter is pressed in search field
        searchTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                searchTemplates(searchTextField.getText());
            }
        });
    }

    public void reset() {
        assocComboBox.getSelectionModel().clearSelection();
        searchTextField.setText(null);
        nicknameTextField.setText(null);
    }

    public void requestFocus() {
        Platform.runLater(() -> searchTextField.requestFocus());
    }

    public Dialog<NamedAssociation> getDialog() {
        if (dialog == null) {

            dialog = new Dialog<>();
            ResourceBundle i18n = toolBox.getI18nBundle();
            dialog.setTitle(i18n.getString("abpane.dialog.title"));
            dialog.setHeaderText(i18n.getString("abpane.dialog.header"));
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().setContent(root);
            dialog.getDialogPane()
                    .getStylesheets()
                    .addAll(toolBox.getStylesheets());
            dialog.setResultConverter(btnType -> {
                NamedAssociation na = null;
                if (btnType == ButtonType.OK) {
                    ConceptAssociationTemplate item = assocComboBox.getSelectionModel().getSelectedItem();
                    String name = nicknameTextField.getText();
                    if (item != null && name != null && !name.isEmpty()) {
                        na = new NamedAssociation(item, name);
                    }
                }
                return na;
            });
            updateControls();

            // --- Disable OK button when values are invalid
            Node node = dialog.getDialogPane().lookupButton(ButtonType.OK);
            node.setDisable(true);
            Runnable r = () -> {
                String nickname = nicknameTextField.getText();
                Boolean isNameEmpty = Optional.ofNullable(nickname)
                        .map(s -> s.trim().isEmpty())
                        .orElse(true);
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

    private void updateControls() {
        toolBox.getServices()
                .getConceptService()
                .findAllTemplates()
                .thenAccept(cats -> {
                    Platform.runLater(() -> {
                        ObservableList<ConceptAssociationTemplate> list = FXCollections.observableArrayList(cats);
                        assocComboBox.setItems(list);
                    });
                });
    }


    public static AssocSelectionDialogController newInstance(UIToolBox toolBox) {
        final ResourceBundle bundle = toolBox.getI18nBundle();
        FXMLLoader loader = new FXMLLoader(AssocSelectionDialogController.class
                .getResource("/fxml/AssociationSelectionPane.fxml"), bundle);
        try {
            loader.load();
            return loader.getController();
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to load AssocSelectionPane from FXML", e);
        }
    }


    private void searchTemplates(String search) {
        List<ConceptAssociationTemplate> templates = assocComboBox.getItems();
        int startIdx = assocComboBox.getSelectionModel().getSelectedIndex() + 1;
        ListUtils.search(search, templates, startIdx, ConceptAssociationTemplate::toString)
            .ifPresent(cat -> assocComboBox.getSelectionModel().select(cat));
    }

}


