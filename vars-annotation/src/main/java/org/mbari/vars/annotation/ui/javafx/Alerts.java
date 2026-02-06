package org.mbari.vars.annotation.ui.javafx;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.messages.*;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Handler for managing alert dialogs. Accepts any ShowAlert message.
 * Instances of ShowExceptionAlert messages will also display the stacktrace.
 *
 * Usage:
 * <pre>
 *     UIToolBox toolBox = ...;
 *     Alerts alerts = new Alerts(toolBox);
 *     alerts.showAlert(new ShowWarningAlert(...));
 *     alerts.showAlert(new ShowFatalErrorAlert(...));
 * </pre>
 * @author Brian Schlining
 * @since 2018-01-11T13:07:00
 */
public class Alerts {

    private final UIToolBox toolBox;
    private final Loggers log = new Loggers(getClass());

    public Alerts(UIToolBox toolBox) {
        this.toolBox = toolBox;
    }

    public void showAlert(ShowAlert msg) {
        log.atDebug().log(() -> "Showing alert: '" + msg.getHeaderText() + "' -> " + msg.getContentText());
        if (msg instanceof ShowFatalErrorAlert) {
            showFatalErrorAlert((ShowFatalErrorAlert) msg);
        }
        else if (msg instanceof ShowExceptionAlert) {
            showExceptionAlert((ShowExceptionAlert) msg);
        }
        else if (msg instanceof ShowWarningAlert) {
            showWarningAlert(msg);
        }
        else {
            showInformationAlert(msg);
        }

    }

    private void showExceptionAlert(ShowExceptionAlert msg) {
        log.atWarn().withCause(msg.getException()).log("Showing an exception alert to the user");
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            buildExeptionAlert(msg, alert);
        });
    }

    private void showFatalErrorAlert(ShowFatalErrorAlert msg) {
        log.atWarn().withCause(msg.getException()).log("Showing a fatal alert to the user");
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            buildExeptionAlert(msg, alert);
        });
    }

    private void showWarningAlert(ShowAlert msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            buildAlert(msg, alert);
        });
    }

    private void showInformationAlert(ShowAlert msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            buildAlert(msg, alert);
        });
    }

    private void buildAlert(ShowAlert msg, Alert alert) {
        alert.getDialogPane().getStylesheets().addAll(toolBox.getStylesheets());
        alert.setTitle(msg.getTitle());
        alert.setHeaderText(msg.getHeaderText());
        alert.setContentText(msg.getContentText());
        alert.getDialogPane()
                .getStylesheets()
                .addAll(toolBox.getStylesheets());
        alert.showAndWait();
    }

    private void buildExeptionAlert(ShowExceptionAlert msg, Alert alert) {
        alert.setTitle(msg.getTitle());
        alert.setHeaderText(msg.getHeaderText());
        alert.setContentText(msg.getContentText());

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        msg.getException().printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane()
                .setExpandableContent(expContent);
        alert.getDialogPane()
                .getStylesheets()
                .addAll(toolBox.getStylesheets());
        alert.showAndWait();
    }
}
