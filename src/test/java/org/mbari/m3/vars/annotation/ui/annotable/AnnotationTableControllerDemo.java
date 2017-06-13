package org.mbari.m3.vars.annotation.ui.annotable;

import com.google.common.collect.Lists;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.ImageReference;
import org.mbari.m3.vars.annotation.ui.DemoConstants;
import org.mbari.vcr4j.time.Timecode;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-06-03T18:38:00
 */
public class AnnotationTableControllerDemo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        UIToolBox toolBox = DemoConstants.getToolBox();
        AnnotationTableController controller = new AnnotationTableController(toolBox);

        Scene scene = new Scene(controller.getTableView());
        scene.getStylesheets().add("/css/common.css");
        scene.getStylesheets().add("/css/annotable.css");

        primaryStage.setScene(scene);
        primaryStage.show();

        final List<Annotation> annotations = makeAnnotations();
        controller.getTableView().setItems(FXCollections.observableList(annotations));

        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }

    private List<Annotation> makeAnnotations() {
        List<Annotation> as = new ArrayList<>();

        Annotation a1 = new Annotation();
        a1.setElapsedTime(Duration.ofMillis(12345));
        a1.setRecordedTimestamp(Instant.now());
        a1.setConcept("Nanomia bijuga");
        a1.setObserver("brian");
        a1.setTimecode(new Timecode(456, 29.97));
        as.add(a1);

        Annotation a2 = new Annotation();
        a2.setRecordedTimestamp(Instant.now());
        a2.setConcept("Pandalus platyceros");
        a2.setObserver("schlin");
        as.add(a2);
        Association a = new Association("sampled-by", "big arm", "nil");
        List<Association> ass = new ArrayList<>();
        ass.add(a);
        a2.setAssociations(ass);

        Annotation ann3 = new Annotation();
        ann3.setRecordedTimestamp(Instant.now());
        ann3.setConcept("Grimpoteuthis");
        ann3.setObserver("schlin");
        as.add(ann3);
        Association a3 = new Association("sampled-by", "big arm", "nil");
        List<Association> ass3 = new ArrayList<>();
        ass3.add(a3);
        ann3.setAssociations(ass3);
        ImageReference ir3 = new ImageReference();
        try {
            ir3.setUrl(new URL("http://www.mbari.org/foo/bar.png"));
            List<ImageReference> images = new ArrayList<>();
            images.add(ir3);
            ann3.setImages(images);
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Annotation ann4 = new Annotation();
        ann4.setElapsedTime(Duration.ofMillis(1111));
        ann4.setConcept("Raja");
        ann4.setObserver("brian");
        Association a4a = new Association("eating", UUID.randomUUID().toString(), "nil");
        Association a4b = new Association("swimming", "self", "nil");
        Association a4c = new Association("surface-color", "self", "red");
        List<Association> ass4 = new ArrayList<>();
        ann4.setAssociations(Lists.newArrayList(a4a, a4b, a4c));
        as.add(ann4);


        return as;
    }
}
