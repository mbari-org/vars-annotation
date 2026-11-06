package org.mbari.vars.annotation.ui.javafx.prefs;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.mbari.vars.annotation.AppVersion;
import org.mbari.vars.annotation.services.raziel.Raziel;
import org.mbari.vars.annotation.ui.Initializer;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.mediaplayers.SettingsPane;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A read-only "About" tab for the preferences dialog showing the app version and
 * details that are useful in bug reports.
 */
public class AboutPaneController implements SettingsPane {

    private final UIToolBox toolBox;
    private final ResourceBundle i18n;
    private final VBox root = new VBox(16);
    private final GridPane detailsGrid = new GridPane();
    private final Label copiedLabel = new Label();
    private final Map<String, String> details = new LinkedHashMap<>();

    public AboutPaneController(UIToolBox toolBox) {
        this.toolBox = toolBox;
        this.i18n = toolBox.getI18nBundle();
        init();
    }

    private void init() {
        var imageView = new ImageView();
        var imageUrl = getClass().getResource("/images/splash/vars-splash.png");
        if (imageUrl != null) {
            imageView.setImage(new Image(imageUrl.toExternalForm()));
        }
        imageView.setFitWidth(120);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        var titleLabel = new Label("VARS Annotation");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        var versionLabel = new Label(i18n.getString("about.version") + " " + AppVersion.version());

        var header = new VBox(4, imageView, titleLabel, versionLabel);
        header.setAlignment(Pos.CENTER);
        AppVersion.copyrightYear().ifPresent(year -> {
            var copyrightLabel = new Label("© " + year + " Monterey Bay Aquarium Research Institute");
            copyrightLabel.setStyle("-fx-font-size: 11px; -fx-opacity: 0.7;");
            header.getChildren().add(copyrightLabel);
        });

        detailsGrid.setHgap(12);
        detailsGrid.setVgap(6);
        var labelColumn = new ColumnConstraints();
        labelColumn.setHalignment(HPos.RIGHT);
        detailsGrid.getColumnConstraints().addAll(labelColumn, new ColumnConstraints());

        // Bug reports are much more useful with exact versions, so make them easy to share
        var copyButton = new Button(i18n.getString("about.copy"));
        copyButton.setOnAction(e -> copyDetails());
        var copyBox = new VBox(6, copyButton, copiedLabel);
        copyBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(header, detailsGrid, copyBox);
        root.setAlignment(Pos.TOP_CENTER);
    }

    /**
     * Refreshes the details each time the dialog opens, e.g. the connection status
     * after the user has fixed their network or server settings.
     */
    @Override
    public void load() {
        // The version is already shown in the header; copyDetails() adds it to the copied text
        details.clear();
        details.put(i18n.getString("about.server"), describeServer());
        details.put("Java", Runtime.version().toString());
        details.put("JavaFX", System.getProperty("javafx.runtime.version", "unknown"));
        details.put(i18n.getString("about.os"), System.getProperty("os.name") + " "
                + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")");

        detailsGrid.getChildren().clear();
        int row = 0;
        for (var entry : details.entrySet()) {
            var name = new Label(entry.getKey() + ":");
            name.setStyle("-fx-opacity: 0.7;");
            var value = new Label(entry.getValue());
            value.setWrapText(true);
            detailsGrid.addRow(row++, name, value);
        }
        copiedLabel.setText("");
    }

    /**
     * Nothing to save. The About tab is read-only.
     */
    @Override
    public void save() {
        // No-op
    }

    @Override
    public String getName() {
        return i18n.getString("about.name");
    }

    @Override
    public Pane getPane() {
        return root;
    }

    private String describeServer() {
        var loginFile = Raziel.LoginFile.load();
        if (loginFile.isEmpty()) {
            return i18n.getString("about.server.notconfigured");
        }
        var url = loginFile.get().url().toString();
        var status = Initializer.getServiceConfigurationError().isPresent()
                ? i18n.getString("about.server.notconnected")
                : i18n.getString("about.server.connected");
        return url + " (" + status + ")";
    }

    private void copyDetails() {
        var text = new StringBuilder("VARS Annotation\n")
                .append(i18n.getString("about.version")).append(": ").append(AppVersion.version()).append("\n");
        details.forEach((k, v) -> text.append(k).append(": ").append(v).append("\n"));
        var content = new ClipboardContent();
        content.putString(text.toString());
        Clipboard.getSystemClipboard().setContent(content);
        copiedLabel.setText(i18n.getString("about.copied"));
    }
}
