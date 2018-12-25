package org.mbari.m3.vars.annotation.ui.imageanno;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ToolBar;

import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.ui.shared.ImageViewExt;
import org.mbari.m3.vars.annotation.util.JFXUtilities;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class PointLayerController implements  LayerController {

    private AnchorPane anchorPane;
    private boolean disable = true;
    private ToolBar toolBar;
    private final UIToolBox toolBox;
    private final String LINK_NAME;
    private final ObservableList<PointLayerNode> points = FXCollections.observableArrayList();

    public PointLayerController(UIToolBox toolBox, StackPane stackPane) {
        this.toolBox = toolBox;
        LINK_NAME = toolBox.getConfig().getString("app.annotation.image.point.linkname");
        // Bind size tto the pane that contains this anchor pane
        getRoot().prefHeightProperty().bind(stackPane.heightProperty());
        getRoot().prefWidthProperty().bind(stackPane.widthProperty());
        points.addListener((ListChangeListener<PointLayerNode>) c -> {
            clear();
            List<Circle> removed = c.getRemoved()
                    .stream()
                    .map(PointLayerNode::getShape)
                    .collect(Collectors.toList());
            List<Circle> added = c.getAddedSubList()
                    .stream()
                    .map(PointLayerNode::getShape)
                    .collect(Collectors.toList());
            AnchorPane r = getRoot();
            r.getChildren().removeAll(removed);
            r.getChildren().addAll(added);
        });
    }

    @Override
    public AnchorPane getRoot() {
        if (anchorPane == null) {
            anchorPane = new AnchorPane();
        }
        return anchorPane;
    }

    @Override
    public void draw(ImageViewExt imageViewExt, List<Association> associations, Color color) {
        List<PointLayerNode> layerNodes = associations.stream()
                .map(a -> PointLayerNode.fromAssociation(imageViewExt, a, LINK_NAME))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(n -> {
                    n.getShape().setFill(color);
                    AnchorPane r = getRoot();
                    r.widthProperty().addListener(n.getResizeChangeListener());
                    r.heightProperty().addListener(n.getResizeChangeListener());
                })
                .collect(Collectors.toList());
        points.addAll(layerNodes);
    }

    @Override
    public void clear() {
        JFXUtilities.runOnFXThread(() -> {
            AnchorPane r = getRoot();
            ObservableList<Node> children = r.getChildren();
            points.forEach(p -> {
                r.widthProperty()
                        .removeListener(p.getResizeChangeListener());
                r.heightProperty()
                        .removeListener(p.getResizeChangeListener());
                children.remove(p.getShape());
            });
            points.clear();
        });
    }

    @Override
    public ToolBar getToolBar() {
        if (toolBar == null) {
            toolBar = new ToolBar();

        }
        return null;
    }




}
