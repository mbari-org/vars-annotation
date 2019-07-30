package org.mbari.vars.ui.javafx.mediadialog;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;
import org.mbari.vars.services.model.Media;


import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-05-31T15:07:00
 */
public class PropertySheetDemo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Media media = new Media();
        media.setAudioCodec("audio/aac");
        media.setVideoCodec("video/mp4");
        media.setCameraId("Tiburon");
        media.setContainer("video/quicktime");
        media.setDescription("");
        media.setFrameRate(30D);
        media.setHeight(1080);
        media.setWidth(1920);
        media.setDuration(Duration.ofMillis(3030303));
        media.setSha512(new byte[]{1, 2, 3});
        media.setSizeBytes(345678L);
        media.setStartTimestamp(Instant.now());
        media.setUri(new URI("http://www.mbari.org"));
        media.setVideoReferenceUuid(UUID.randomUUID());
        media.setVideoSequenceName("Tiburon 20160606");
        media.setVideoSequenceUuid(UUID.randomUUID());
        media.setVideoUuid(UUID.randomUUID());
        media.setVideoName("Tiburon 20160606T012345Z");
        ObservableList<PropertySheet.Item> props = BeanPropertyUtils.getProperties(media);
        PropertySheet ps = new PropertySheet(props);
        Scene scene = new Scene(ps);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
