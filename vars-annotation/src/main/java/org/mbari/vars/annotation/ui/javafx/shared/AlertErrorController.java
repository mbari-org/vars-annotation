package org.mbari.vars.annotation.ui.javafx.shared;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.mbari.vars.annotation.ui.UIToolBox;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Brian Schlining
 * @since 2017-06-12T11:42:00
 */
public class AlertErrorController {

    private final Alert alert ;
    private GridPane content;
    private TextArea textArea;
    private final UIToolBox toolBox;

    public AlertErrorController(Alert.AlertType alertType, UIToolBox toolBox) {
        this.alert = new Alert(alertType);
        this.toolBox = toolBox;
    }

    public void showAndWait(String title, String headerText, String contentText, Exception ex) {
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.getDialogPane().setExpandableContent(getContent(ex));
        alert.showAndWait();
    }

    private GridPane getContent(Exception ex) {
        if (content == null) {
            // TODO i18n
            Label label = new Label(toolBox.getI18nBundle().getString("shared.stacktrace.msg"));

            textArea = new TextArea();
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            content = new GridPane();
            content.setMaxWidth(Double.MAX_VALUE);
            content.add(label, 0, 0);
            content.add(textArea, 0 , 1);

            content = new GridPane();
        }
        // Make pretty stack trace string
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();
        textArea.setText(exceptionText);
        return content;
    }



}
