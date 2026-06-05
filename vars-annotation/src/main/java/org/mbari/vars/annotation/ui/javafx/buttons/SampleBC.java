package org.mbari.vars.annotation.ui.javafx.buttons;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.mbari.vars.annotation.etc.rxjava.EventBus;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.commands.CreateAssociationsCmd;
import org.mbari.vars.annotation.ui.javafx.Icons;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;
import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.ui.javafx.shared.FilteredComboBoxDecorator;
import org.mbari.vars.annotation.ui.messages.ShowExceptionAlert;
import org.mbari.vars.annotation.ui.messages.ShowWarningAlert;
import org.mbari.vars.annotation.ui.util.JFXUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Button Controller for adding a sample to an annotation. A sample has
 * 2 associations, one for the sampler, the other for the sample id.
 * @author Brian Schlining
 * @since 2017-09-11T08:57:00
 */
public class SampleBC extends AbstractBC {

    private Dialog<Pair<String, String>> dialog;
    private GridPane dialogPane;
    private ComboBox<String> comboBox;
    private TextField textField;
    private ProgressIndicator loadingIndicator;
    private boolean samplersLoaded = false;
    private final Loggers log = new Loggers(getClass());
    // HACK To track selected sampler after combox is hidden: misbehaving Filter
    private volatile String lastSelectedSampler;


    public SampleBC(Button button, UIToolBox toolBox) {
        super(button, toolBox);
    }

    public void init() {
        ResourceBundle i18n = toolBox.getI18nBundle();
        String tooltip = i18n.getString("buttons.sample");
        Text icon = Icons.ADD_SHOPPING_CART.standardSize();
        initializeButton(tooltip, icon);
    }

    @Override
    protected void apply() {
        JFXUtilities.runOnFXThread(() -> {
            var d = getDialog();
            if (comboBox != null) {
                comboBox.requestFocus();
            }
            Optional<Pair<String, String>> v = d.showAndWait();
            v.ifPresent(pair -> createAssociation(pair.getKey(), pair.getValue()));
            if (textField != null) {
                textField.setText(null);
            }
            if (comboBox != null) {
                String concept = lastSelectedSampler == null ?
                        toolBox.getAppConfig().getAppAnnotationSampleDefaultConcept() :
                        lastSelectedSampler;
                comboBox.getSelectionModel().select(concept);
            }
        });
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
            dialog.setResizable(true);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().setContent(getDialogPane());
            dialog.getDialogPane().getStylesheets().addAll(toolBox.getStylesheets());
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return new Pair<>(lastSelectedSampler, textField.getText());
                }
                return null;
            });
            // Keep OK disabled until the sampler list has finished loading.
            var okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            if (okButton != null) {
                okButton.setDisable(!samplersLoaded);
            }
        }
        return dialog;
    }

    private void createAssociation(String sampleBy, String sampleId) {
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
            dialogPane.setHgap(8);
            dialogPane.setVgap(8);

            comboBox = new ComboBox<>();
            comboBox.setEditable(false);
            comboBox.setVisible(false);
            comboBox.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((obs) -> {
                        String item = comboBox.getSelectionModel().getSelectedItem();
                        log.atDebug().log(() -> "Sampler: " + item);
                        // HACK To track selected sampler after combox is hidden: misbehaving Filter
                        if (item != null) {
                            lastSelectedSampler = item;
                        }
                    });

            loadingIndicator = new ProgressIndicator();
            loadingIndicator.setMaxSize(24, 24);

            textField = new TextField();
            String by = toolBox.getI18nBundle().getString("buttons.sample.dialog.label.by");
            String id = toolBox.getI18nBundle().getString("buttons.sample.dialog.label.id");
            textField.setPromptText(id);

            // Stack the spinner and combobox in the same cell; spinner shown until data arrives.
            var samplerContainer = new StackPane(loadingIndicator, comboBox);
            dialogPane.add(new Label(by), 0, 0);
            dialogPane.add(samplerContainer, 1, 0);
            dialogPane.add(new Label(id), 0, 1);
            dialogPane.add(textField, 1, 1);

            loadSamplers();
        }
        return dialogPane;
    }

    private void loadSamplers() {
        String defaultSampleConcept = toolBox.getAppConfig().getAppAnnotationSampleDefaultConcept();
        ResourceBundle i18n = toolBox.getI18nBundle();
        String title = i18n.getString("buttons.sample.dialog.title");
        String header = i18n.getString("buttons.sample.dialog.header");
        String content = i18n.getString("buttons.sample.warning.content");

        toolBox.getServices()
                .conceptService()
                .findPhylogenyDown(defaultSampleConcept)
                .thenAccept(opt -> JFXUtilities.runOnFXThread(() -> {
                    if (opt.isPresent()) {
                        var samplers = opt.get().flatten();
                        new FilteredComboBoxDecorator<>(comboBox, FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);
                        comboBox.setItems(FXCollections.observableArrayList(samplers));
                        comboBox.getSelectionModel().select(defaultSampleConcept);

                        loadingIndicator.setVisible(false);
                        comboBox.setVisible(true);
                        samplersLoaded = true;

                        if (dialog != null) {
                            var okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
                            if (okButton != null) {
                                okButton.setDisable(false);
                            }
                            // Resize to fit the now-populated combobox if dialog is showing.
                            var scene = dialog.getDialogPane().getScene();
                            if (scene != null && scene.getWindow() != null && scene.getWindow().isShowing()) {
                                scene.getWindow().sizeToScene();
                            }
                        }
                    } else {
                        toolBox.getEventBus().send(new ShowWarningAlert(title, header, content));
                    }
                }))
                .exceptionally(ex -> {
                    JFXUtilities.runOnFXThread(() ->
                            toolBox.getEventBus().send(new ShowExceptionAlert(title, header, content,
                                    ex instanceof Exception e ? e : new RuntimeException(ex))));
                    return null;
                });
    }
}
