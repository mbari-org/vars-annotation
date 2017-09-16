package org.mbari.m3.vars.annotation.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;

/**
 *
 */
public class BulkEditorPaneController {

    private UIToolBox toolBox;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private VBox root;

    @FXML
    private JFXButton refreshButton;

    @FXML
    private JFXButton moveFramesButton;

    @FXML
    private JFXButton renameObservationsButton;

    @FXML
    private JFXButton deleteObservationsButton;

    @FXML
    private JFXButton addAssociationButton;

    @FXML
    private JFXButton replaceAssociationButton;

    @FXML
    private JFXButton deleteAssociationButton;

    @FXML
    private JFXButton searchButton;

    @FXML
    private JFXComboBox<?> groupComboBox;

    @FXML
    private JFXComboBox<?> activityComboBox;

    @FXML
    private Label groupLabel;

    @FXML
    private Label activityLabel;

    @FXML
    void initialize() {


        // --- Configure buttons
        GlyphsFactory gf = MaterialIconFactory.get();
        ResourceBundle i18n = toolBox.getI18nBundle();

        Text refreshIcon = gf.createIcon(MaterialIcon.REFRESH, "30px");
        refreshButton.setGraphic(refreshIcon);
        refreshButton.setDisable(true);
        refreshButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.refresh.tooltip")));

        Image moveAnnoImg = new Image(getClass()
                .getResource("/images/buttons/row_replace.png").toExternalForm());
        moveFramesButton.setGraphic(new ImageView(moveAnnoImg));
        moveFramesButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.annotation.move.tooltip")));

        Image editAnnoImg = new Image(getClass()
                .getResource("/images/buttons/row_edit.png").toExternalForm());
        renameObservationsButton.setGraphic(new ImageView(editAnnoImg));
        renameObservationsButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.annotation.rename.tooltip")));

        Image deleteAnnoImg = new Image(getClass()
                .getResource("/images/buttons/row_delete.png").toExternalForm());
        deleteObservationsButton.setGraphic(new ImageView(deleteAnnoImg));
        deleteObservationsButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.annotation.delete.tooltip")));

        Image addAssImg = new Image(getClass()
                .getResource("/images/buttons/branch_add.png").toExternalForm());
        addAssociationButton.setGraphic(new ImageView(addAssImg));
        addAssociationButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.association.add.tooltip")));

        Image editAssImg = new Image(getClass()
                .getResource("/images/buttons/branch_edit.png").toExternalForm());
        replaceAssociationButton.setGraphic(new ImageView(editAssImg));
        replaceAssociationButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.association.edit.tooltip")));

        Image deleteAssImg = new Image(getClass()
                .getResource("/images/buttons/branch_delete.png").toExternalForm());
        deleteAssociationButton.setGraphic(new ImageView(deleteAssImg));
        deleteAssociationButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.association.delete.tooltip")));

        Text searchIcon = gf.createIcon(MaterialIcon.SEARCH, "30px");
        searchButton.setText(null);
        searchButton.setGraphic(searchIcon);
        searchButton.setTooltip(new Tooltip(i18n.getString("bulkeditor.search.button")));

    }

    public VBox getRoot() {
        return root;
    }

    public static BulkEditorPaneController newInstance(UIToolBox toolBox) {
        final ResourceBundle bundle = Initializer.getToolBox().getI18nBundle();
        FXMLLoader loader = new FXMLLoader(BulkEditorPaneController.class
                .getResource("/fxml/BulkEditorPane.fxml"), bundle);
        try {
            loader.load();
            BulkEditorPaneController controller = loader.getController();
            controller.toolBox = toolBox;
            return controller;
        }
        catch (Exception e) {
            throw  new RuntimeException("Failed to load BulkEditorPane fro FXML", e);
        }
    }

    private void refresh() {

        ObservableList<Annotation> annotations = toolBox.getData().getAnnotations();

        List<String> concepts = annotations.stream()
                .map(Annotation::getConcept)
                .distinct()
                .collect(Collectors.toList());

        List<Association> associations = annotations.stream()
                .map(Annotation::getAssociations)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        // TODO set values in comboboxs
hr

    }

    private void needsRefresh() {
        refreshButton.setDisable(false);
    }
}
