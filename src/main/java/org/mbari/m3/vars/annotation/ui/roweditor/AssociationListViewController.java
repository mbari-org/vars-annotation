package org.mbari.m3.vars.annotation.ui.roweditor;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import java.net.URL;
import java.util.ResourceBundle;

import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class AssociationListViewController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private JFXListView<?> listView;

    @FXML
    private JFXButton addButton;

    @FXML
    private JFXButton editButton;

    @FXML
    private JFXButton cancelButton;

    @FXML
    void onAdd(ActionEvent event) {
        // show
    }

    @FXML
    void onCancel(ActionEvent event) {

    }

    @FXML
    void onEdit(ActionEvent event) {

    }

    @FXML
    void initialize() {
        GlyphsFactory gf = MaterialIconFactory.get();

    }
}
