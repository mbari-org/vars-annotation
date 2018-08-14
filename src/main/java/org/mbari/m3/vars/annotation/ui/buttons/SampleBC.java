package org.mbari.m3.vars.annotation.ui.buttons;

import com.jfoenix.controls.JFXComboBox;
import com.typesafe.config.Config;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.CreateAssociationsCmd;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.User;
import org.mbari.m3.vars.annotation.ui.shared.FilteredComboBoxDecorator;
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
        MaterialIconFactory iconFactory = MaterialIconFactory.get();
        Text icon = iconFactory.createIcon(MaterialIcon.NATURE_PEOPLE, "30px");
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
                toolBox.getConfig().getString("app.annotation.sample.default.concept") :
                lastSelectedSampler;
        comboBox.getSelectionModel().select(concept);
    }


    private Dialog<Pair<String, String>> getDialog() {
        if (dialog == null) {
            ResourceBundle i18n = toolBox.getI18nBundle();
            MaterialIconFactory iconFactory = MaterialIconFactory.get();
            Text icon = iconFactory.createIcon(MaterialIcon.NATURE_PEOPLE, "30px");
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
        Association a1 = new Association(config.getString("app.annotation.sample.association.equipment"),
                sampleBy, Association.VALUE_NIL);
        Association a2 = new Association(config.getString("app.annotation.sample.association.reference"),
                Association.VALUE_SELF, sampleId);
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


            String defaultSampleConcept = toolBox.getConfig()
                    .getString("app.annotation.sample.default.concept");

            // TODO listen for cache reset to clear and repopulate dialog
            toolBox.getServices()
                    .getConceptService()
                    .findConcept(defaultSampleConcept)
                    .thenAccept(opt -> {
                        if (opt.isPresent()) {
                            List<String> samplers = opt.get().flatten();
                            new FilteredComboBoxDecorator<>(comboBox, FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);
                            comboBox.setItems(FXCollections.observableArrayList(samplers));
                            comboBox.getSelectionModel().select(defaultSampleConcept);
                        }
                        else {
                            // TODO show alert
                        }
                    });

        }
        return dialogPane;
    }
}
