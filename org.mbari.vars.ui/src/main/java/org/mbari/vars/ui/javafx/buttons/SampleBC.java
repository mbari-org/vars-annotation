package org.mbari.vars.ui.javafx.buttons;

import com.jfoenix.controls.JFXComboBox;
import com.typesafe.config.Config;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.commands.CreateAssociationsCmd;
import org.mbari.vars.ui.javafx.Icons;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.ui.javafx.shared.FilteredComboBoxDecorator;
import org.mbari.vars.ui.messages.ShowExceptionAlert;
import org.mbari.vars.ui.messages.ShowWarningAlert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-09-11T08:57:00
 */
public class SampleBC extends AbstractBC {

    private Dialog<Pair<String, String>> dialog;
    private GridPane dialogPane;
    private ComboBox<String> comboBox;
    private TextField textField;
    private final Logger log = LoggerFactory.getLogger(getClass());
    // HACK To track selected sampler after combox is hidden: misbehaving Filter
    private volatile String lastSelectedSampler;


    public SampleBC(Button button, UIToolBox toolBox) {
        super(button, toolBox);
    }

    public void init() {
        ResourceBundle i18n = toolBox.getI18nBundle();
        String tooltip = i18n.getString("buttons.sample");
//        Text icon = iconFactory.createIcon(MaterialIcon.ADD_SHOPPING_CART, "30px");
        Text icon = Icons.ADD_SHOPPING_CART.standardSize();
        initializeButton(tooltip, icon);
    }

    @Override
    protected void apply() {
        Platform.runLater(() -> comboBox.requestFocus());
        Optional<Pair<String, String>> v = getDialog().showAndWait();
        if (v.isPresent()) {
            Pair<String, String> pair = v.get();
            createAssociation(pair.getKey(), pair.getValue());
        }
        textField.setText(null);

        String concept = lastSelectedSampler == null ?
                toolBox.getAppConfig().getAppAnnotationSampleDefaultConcept() :
                lastSelectedSampler;
        comboBox.getSelectionModel().select(concept);
    }


    private Dialog<Pair<String, String>> getDialog() {
        if (dialog == null) {
            ResourceBundle i18n = toolBox.getI18nBundle();
            Text icon = Icons.NATURE_PEOPLE.standardSize();
            dialog = new Dialog<>();
            dialog.setTitle(i18n.getString("buttons.sample.dialog.title"));
            dialog.setHeaderText(i18n.getString("buttons.sample.dialog.header"));
            dialog.setContentText(i18n.getString("buttons.sample.dialog.content"));
            dialog.setGraphic(icon);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().setContent(getDialogPane());
            dialog.getDialogPane().getStylesheets().addAll(toolBox.getStylesheets());
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return new Pair<>(lastSelectedSampler,
                            textField.getText());
                }
                return null;
            });
        }
        return dialog;
    }

    private void createAssociation(String sampleBy, String sampleId) {
        Config config = toolBox.getConfig();
        String assNameEquipment = toolBox.getAppConfig()
                .getAppAnnotationSampleAssociationEquipment();
        Association a1 = new Association(assNameEquipment, sampleBy, Association.VALUE_NIL);
        String assNameReference = toolBox.getAppConfig().getAppAnnotationSampleAssociationReference();
        Association a2 = new Association(assNameReference, Association.VALUE_SELF, sampleId);
        EventBus eventBus = toolBox.getEventBus();
        List<Annotation> selectedAnnotations = new ArrayList<>(toolBox.getData().getSelectedAnnotations());
        CreateAssociationsCmd cmd1 = new CreateAssociationsCmd(a1, selectedAnnotations);
        CreateAssociationsCmd cmd2 = new CreateAssociationsCmd(a2, selectedAnnotations);
        eventBus.send(cmd1);
        eventBus.send(cmd2);
    }

    private GridPane getDialogPane() {
        if (dialogPane == null) {
            dialogPane = new GridPane();
            dialogPane.setPadding(new Insets(20, 10, 10, 10));
            comboBox = new JFXComboBox<>();
            comboBox.setEditable(false);
            comboBox.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((obs) -> {
                        String item = comboBox.getSelectionModel().getSelectedItem();
                        log.debug("Sampler: " + item);
                        // HACK To track selected sampler after combox is hidden: misbehaving Filter
                        if (item != null) {
                            lastSelectedSampler = item;
                        }
                    });
            textField = new TextField();
            String by = toolBox.getI18nBundle().getString("buttons.sample.dialog.label.by");
            String id = toolBox.getI18nBundle().getString("buttons.sample.dialog.label.id");
            textField.setPromptText(id);
            dialogPane.add(new Label(by), 0, 0);
            dialogPane.add(comboBox, 1, 0);
            dialogPane.add(new Label(id), 0, 1);
            dialogPane.add(textField, 1, 1);

            String defaultSampleConcept = toolBox.getAppConfig()
                    .getAppAnnotationSampleDefaultConcept();

            ResourceBundle i18n = toolBox.getI18nBundle();
            String title = i18n.getString("buttons.sample.dialog.title");
            String header = i18n.getString("buttons.sample.dialog.header");
            String content = i18n.getString("buttons.sample.warning.content");

            // TODO listen for cache reset to clear and repopulate dialog
            toolBox.getServices()
                    .getConceptService()
                    .findConcept(defaultSampleConcept)
                    .handle((opt, ex) -> {
                        if (ex instanceof Exception) {
                            toolBox.getEventBus()
                                    .send(new ShowExceptionAlert(title, header, content, (Exception) ex));
                        }
                        else if (opt.isEmpty()) {
                            toolBox.getEventBus()
                                    .send(new ShowWarningAlert(title, header, content));
                        }
                        else {
                            List<String> samplers = opt.get().flatten();
                            new FilteredComboBoxDecorator<>(comboBox, FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);
                            comboBox.setItems(FXCollections.observableArrayList(samplers));
                            comboBox.getSelectionModel().select(defaultSampleConcept);
                        }
                        return null;
                    });

        }
        return dialogPane;
    }
}
