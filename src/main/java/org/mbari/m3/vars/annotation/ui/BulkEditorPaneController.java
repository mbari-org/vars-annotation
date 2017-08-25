package org.mbari.m3.vars.annotation.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import java.net.URL;
import java.util.ResourceBundle;

import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;

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
        GlyphsFactory gf = MaterialIconFactory.get();

        Text refreshIcon = gf.createIcon(MaterialIcon.REFRESH, "30px");
        refreshButton.setGraphic(refreshIcon);

        Image moveAnnoImg = new Image(getClass()
                .getResource("/images/buttons/row_replace.png").toExternalForm());
        moveFramesButton.setGraphic(new ImageView(moveAnnoImg));

        Image editAnnoImg = new Image(getClass()
                .getResource("/images/buttons/row_edit.png").toExternalForm());
        renameObservationsButton.setGraphic(new ImageView(editAnnoImg));

        Image deleteAnnoImg = new Image(getClass()
                .getResource("/images/buttons/row_delete.png").toExternalForm());
        deleteObservationsButton.setGraphic(new ImageView(deleteAnnoImg));

        Image addAssImg = new Image(getClass()
                .getResource("/images/buttons/branch_add.png").toExternalForm());
        addAssociationButton.setGraphic(new ImageView(addAssImg));

        Image editAssImg = new Image(getClass()
                .getResource("/images/buttons/branch_edit.png").toExternalForm());
        replaceAssociationButton.setGraphic(new ImageView(editAssImg));

        Image deleteAssImg = new Image(getClass()
                .getResource("/images/buttons/branch_delete.png").toExternalForm());
        deleteAssociationButton.setGraphic(new ImageView(deleteAssImg));

        Text searchIcon = gf.createIcon(MaterialIcon.SEARCH, "30px");
        searchButton.setText(null);
        searchButton.setGraphic(searchIcon);

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
}
