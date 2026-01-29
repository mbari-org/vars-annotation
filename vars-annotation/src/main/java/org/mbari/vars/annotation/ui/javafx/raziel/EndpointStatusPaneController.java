package org.mbari.vars.annotation.ui.javafx.raziel;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.mbari.vars.services.model.EndpointStatus;
import org.mbari.vars.annotation.ui.javafx.Icons;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EndpointStatusPaneController {
    private final ObjectProperty<EndpointStatus> endpointStatuses = new SimpleObjectProperty<>();
    private final HBox root = new HBox();
    private final Label nameLabel = new Label();
    private final Label statusLabel = new Label();
    private final Tooltip urlTooltip = new Tooltip();
    private final Text okIcon = Icons.CHECK.standardSize();
    private final Text failIcon = Icons.CLEAR.standardSize();

    public EndpointStatusPaneController() {
        init();
    }

    public HBox getRoot() {
        return root;
    }

    private void init() {
        nameLabel.setTooltip(urlTooltip);
        root.setAlignment(Pos.CENTER_LEFT);
        root.setSpacing(10);
        root.getChildren().addAll(statusLabel, nameLabel);
        okIcon.setStroke(Color.GREEN);
        failIcon.setStroke(Color.RED);
        endpointStatuses.addListener(((observable, oldValue, newValue) -> update(newValue)));
    }

    public void update(EndpointStatus es) {
        if (es.isHealthy()) {
            if (es.getEndpointConfig().getSecret() != null) {
                updateAsOK(es);
            }
            else {
                updateAsBadAuth(es);
            }
        }
        else {
            updateAsFail(es);
        }
    }

    private void updateAsOK(EndpointStatus es) {
        var healthStatus = es.getHealthStatusCheck().getHealthStatus();
        statusLabel.setGraphic(okIcon);
        statusLabel.setTextFill(Color.GREEN);
        statusLabel.setText(null);
        var s = String.format("%s - %s v%s on JDK %s",
                healthStatus.getDescription(),
                es.getEndpointConfig().getName(),
                healthStatus.getVersion(),
                healthStatus.getJdkVersion());
        nameLabel.setText(s);
        urlTooltip.setText(es.getEndpointConfig().getUrl().toExternalForm());
    }

    private void updateAsBadAuth(EndpointStatus es) {
        var healthStatus = es.getHealthStatusCheck().getHealthStatus();
        statusLabel.setGraphic(failIcon);
        statusLabel.setText("ACCESS DENIED");
        statusLabel.setTextFill(Color.RED);
        var s = String.format("%s - %s v%s on JDK %s",
                healthStatus.getDescription(),
                es.getEndpointConfig().getName(),
                healthStatus.getVersion(),
                healthStatus.getJdkVersion());
        nameLabel.setText(s);
        urlTooltip.setText(es.getEndpointConfig().getUrl().toExternalForm());
    }

    private void updateAsFail(EndpointStatus es) {
        statusLabel.setGraphic(failIcon);
        statusLabel.setText("NOT FOUND");
        statusLabel.setTextFill(Color.RED);
        nameLabel.setText(es.getEndpointConfig().getName());
        urlTooltip.setText(null);
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

    public static List<EndpointStatusPaneController> from(Collection<EndpointStatus> statuses) {
        return statuses.stream()
                .map(es -> {
                    var pane = new EndpointStatusPaneController();
                    Platform.runLater(() -> pane.update(es));
                    return pane;
                })
                .collect(Collectors.toList());
    }
}
