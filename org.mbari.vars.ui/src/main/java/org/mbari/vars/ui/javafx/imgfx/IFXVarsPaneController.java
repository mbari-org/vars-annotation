package org.mbari.vars.ui.javafx.imgfx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.javafx.mediadialog.MediaPaneController;
import org.mbari.vars.ui.util.FXMLUtils;

import java.net.URL;
import java.util.ResourceBundle;

// Model server front end: https://adamant.tator.io:8082/
// https://stackoverflow.com/questions/44432343/javafx-scrollpane-in-splitpane-cannot-scroll
public class IFXVarsPaneController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private CheckBox addCommentCb;

    @FXML
    private CheckBox annoExistingCb;

    @FXML
    private ListView<?> annoListView;

    @FXML
    private ScrollPane annoScrollPane;

    @FXML
    private ListView<?> imageListView;

    @FXML
    private ScrollPane imageScrollPane;

    @FXML
    private ImageView imageView;

    @FXML
    private Spinner<?> magnificationSpinner;

    @FXML
    private Button mlButton;

    @FXML
    private TextField mlUrlTextField;

    @FXML
    private VBox root;

    private IFXToolBox toolBox;

    @FXML
    void initialize() {

    }

    public static IFXVarsPaneController newInstance(IFXToolBox toolBox) {
            ResourceBundle i18n = Initializer.getToolBox().getI18nBundle();
            var controller = FXMLUtils.newInstance(IFXVarsPaneController.class,
                    "/fxml/IFXVarsPane.fxml",
                    i18n);
            controller.toolBox = toolBox;
            return controller;
        }
    }
}


