package org.mbari.m3.vars.annotation.mediaplayers.sharktopoda;


import java.util.function.UnaryOperator;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import org.mbari.m3.vars.annotation.ui.prefs.IPrefs;
import org.mbari.m3.vars.annotation.util.FXMLUtil;

/**
 * @author Brian Schlining
 * @since 2017-08-08T15:21:00
 */
public class SharktopodaPaneController implements IPrefs {

    @FXML
    private TextField controlPortTextField;

    @FXML
    private TextField framegrabPortTextField;

    @FXML
    private GridPane root;

    @FXML
    void initialize() {

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getText();
            if (text.matches("[0-9]*")) {
                return change;
            }
            return null;
        };

        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        controlPortTextField.setTextFormatter(textFormatter);
        framegrabPortTextField.setTextFormatter(textFormatter);
    }

    /**
     *
     * @return Pair with Sharkopoda control port and Framecapture port
     */
    public Pair<Integer, Integer> getPortNumbers() {
        try {
            int cport = Integer.parseInt(controlPortTextField.getText());
            int fport = Integer.parseInt(framegrabPortTextField.getText());
            return new Pair<>(cport, fport);
        }
        catch (Exception e) {
            return null;
        }
    }

    public static SharktopodaPaneController newInstance() {
        return FXMLUtil.newInstance(SharktopodaPaneController.class,
                "/fxml/SharktopodaPane.fxml");
    }

    public GridPane getRoot() {
        return root;
    }

    @Override
    public void load() {

    }

    @Override
    public void save() {

    }
}
