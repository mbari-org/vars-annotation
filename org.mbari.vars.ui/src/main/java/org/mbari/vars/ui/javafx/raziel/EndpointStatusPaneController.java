package org.mbari.vars.ui.javafx.raziel;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.mbari.vars.services.model.EndpointStatus;
import org.mbari.vars.services.model.HealthStatusCheck;
import org.mbari.vars.ui.javafx.Icons;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EndpointStatusPaneController {
    private final ObjectProperty<EndpointStatus> endpointStatuses = new SimpleObjectProperty<>();
    private VBox root = new VBox();
    private Label nameLabel = new Label();
    private Label statusLabel = new Label();
    private Label urlLabel = new Label();
    private Text okIcon = Icons.CHECK.standardSize();
    private Text failIcon = Icons.CLEAR.standardSize();

    public EndpointStatusPaneController() {
        init();
    }

    public VBox getRoot() {
        return root;
    }

    private void init() {
        var hbox = new HBox();
        hbox.getChildren().addAll(statusLabel, nameLabel);
        root.getChildren().addAll(hbox, urlLabel);
        okIcon.setFill(Color.GREEN);
        failIcon.setFill(Color.RED);
        endpointStatuses.addListener(((observable, oldValue, newValue) -> update(newValue)));
    }

    public void update(EndpointStatus es) {
        if (es.isHealthy()) {
            updateAsOK(es);
        }
        else {
            updateAsFail(es);
        }
    }

    private void updateAsOK(EndpointStatus es) {
        var healthStatus = es.getHealthStatusCheck().getHealthStatus();
        statusLabel.setGraphic(okIcon);
        var s = String.format("%s v%s on JDK %s",
                es.getEndpointConfig().getName(),
                healthStatus.getVersion(),
                healthStatus.getJdkVersion());
        nameLabel.setText(s);
        urlLabel.setText(es.getEndpointConfig().getUrl().toExternalForm());
    }

    private void updateAsFail(EndpointStatus es) {
        statusLabel.setGraphic(failIcon);
        nameLabel.setText(es.getEndpointConfig().getName());
        urlLabel.setText(null);
    }


    public EndpointStatus getEndpointStatuses() {
        return endpointStatuses.get();
    }

    public ObjectProperty<EndpointStatus> endpointStatusesProperty() {
        return endpointStatuses;
    }

    public void setEndpointStatuses(EndpointStatus endpointStatuses) {
        this.endpointStatuses.set(endpointStatuses);
    }

    public static List<EndpointStatusPaneController> from(Set<EndpointStatus> statuses) {
        return statuses.stream()
                .map(es -> {
                    var pane = new EndpointStatusPaneController();
                    Platform.runLater(() -> pane.update(es));
                    return pane;
                })
                .collect(Collectors.toList());
    }
}
