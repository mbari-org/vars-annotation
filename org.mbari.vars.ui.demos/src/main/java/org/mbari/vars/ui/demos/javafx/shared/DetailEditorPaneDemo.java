package org.mbari.vars.ui.demos.javafx.shared;

import com.google.common.collect.Lists;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.mbari.vars.ui.Initializer;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.services.model.ConceptAssociationTemplate;
import org.mbari.vars.services.model.Details;
import org.mbari.vars.ui.javafx.shared.DetailEditorPaneController;

import java.util.List;


/**
 * @author Brian Schlining
 * @since 2018-11-29T15:19:00
 */
public class DetailEditorPaneDemo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        UIToolBox toolBox = Initializer.getToolBox();
        DetailEditorPaneController controller = DetailEditorPaneController.newInstance(toolBox);
        GridPane root = controller.getRoot();
        root.setPadding(new Insets(10));
        Scene scene = new Scene(root);
        scene.getStylesheets().addAll(toolBox.getStylesheets());
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(evt -> System.exit(0));
        Thread t = new Thread(() -> {
            List<Details> templates = Lists.newArrayList(ConceptAssociationTemplate.NIL,
                    new ConceptAssociationTemplate("linkname", "toconcept", "linkvalue"),
                    new ConceptAssociationTemplate("a really, really, long details/association",
                            "Nanomia bijuga",
                            "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
            int i = 0;
            while (true) {
                controller.setDetails(templates.get(i));
                if (i >= templates.size() - 1) {
                    i = 0;
                }
                else {
                    i++;
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // Do nothing
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }
}
