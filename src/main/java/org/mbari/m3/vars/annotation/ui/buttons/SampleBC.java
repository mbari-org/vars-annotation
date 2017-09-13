package org.mbari.m3.vars.annotation.ui.buttons;

import com.jfoenix.controls.JFXComboBox;
import com.typesafe.config.Config;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
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

import java.util.List;
import java.util.Optional;

/**
 * @author Brian Schlining
 * @since 2017-09-11T08:57:00
 */
public class SampleBC {

    private final Button button;
    private final UIToolBox toolBox;
    private final Dialog<Pair<String, String>> dialog;
    private GridPane dialogPane;
    private ComboBox<String> comboBox;
    private TextField textField;

    public SampleBC(Button button, UIToolBox toolBox) {
        this.button = button;
        this.toolBox = toolBox;
        dialog = new Dialog<>();
        init();
    }

    public void init() {
        button.setTooltip(new Tooltip(toolBox.getI18nBundle().getString("buttons.sample")));
        MaterialIconFactory iconFactory = MaterialIconFactory.get();
        Text icon = iconFactory.createIcon(MaterialIcon.NATURE_PEOPLE, "30px");
        button.setText(null);
        button.setGraphic(icon);
        button.setDisable(true);

        dialog.setTitle(null);
        dialog.setHeaderText(null);
        dialog.setGraphic(icon);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(getDialogPane());
        dialog.getDialogPane().getStylesheets().addAll(toolBox.getStylesheets());

        button.setOnAction(e -> {
            Optional<Pair<String, String>> v = dialog.showAndWait();
            if (v.isPresent()) {
                Pair<String, String> pair = v.get();
                createAssociation(pair.getKey(), pair.getValue());
            }
            textField.setText(null);
            String defaultSampleConcept = toolBox.getConfig()
                    .getString("app.annotation.sample.default.concept");
            comboBox.getSelectionModel().select(defaultSampleConcept);
        });

        toolBox.getEventBus()
                .toObserverable()
                .ofType(AnnotationsSelectedEvent.class)
                .subscribe(e -> {
                   User user = toolBox.getData().getUser();
                   boolean enabled = (user != null) && e.get().size() > 0;
                   button.setDisable(!enabled);
                });
    }

    private void createAssociation(String sampleBy, String sampleId) {
        Config config = toolBox.getConfig();
        Association a1 = new Association(config.getString("app.annotation.sample.association.equipment"),
                sampleBy, Association.VALUE_NIL);
        Association a2 = new Association(config.getString("app.annotation.sample.association.reference"),
                Association.VALUE_SELF, sampleId);
        EventBus eventBus = toolBox.getEventBus();
        ObservableList<Annotation> selectedAnnotations = toolBox.getData().getSelectedAnnotations();
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
                            comboBox.getItems().addAll(samplers);
                            comboBox.getSelectionModel().select(defaultSampleConcept);
                            new FilteredComboBoxDecorator<>(comboBox, FilteredComboBoxDecorator.STARTSWITH);

                            dialog.setResultConverter(dialogButton -> {
                                if (dialogButton == ButtonType.OK) {
                                    return new Pair<>(comboBox.getSelectionModel().getSelectedItem(),
                                            textField.getText());
                                }
                                return null;
                            });
                        }
                        else {
                            // TODO show alert
                        }
                    });

        }
        return dialogPane;
    }
}
