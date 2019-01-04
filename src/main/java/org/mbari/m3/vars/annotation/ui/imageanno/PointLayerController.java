package org.mbari.m3.vars.annotation.ui.imageanno;

import com.jfoenix.controls.JFXComboBox;
import de.jensd.fx.glyphs.GlyphsFactory;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ToolBar;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.ui.shared.ImageViewExt;
import org.mbari.m3.vars.annotation.util.JFXUtilities;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PointLayerController implements  LayerController {

    private final StackPane stackPane;
    private AnchorPane anchorPane;
    private boolean disable = true;
    private ToolBar toolBar;
    private final UIToolBox toolBox;
    private final String LINK_NAME;
    private final ObservableList<PointLayerNode> points = FXCollections.observableArrayList();

    public PointLayerController(UIToolBox toolBox, StackPane stackPane) {
        this.toolBox = toolBox;
        this.stackPane = stackPane;
        LINK_NAME = toolBox.getConfig().getString("app.annotation.image.point.linkname");
        // Bind size tto the pane that contains this anchor pane
        getRoot().prefHeightProperty().bind(stackPane.heightProperty());
        getRoot().prefWidthProperty().bind(stackPane.widthProperty());
        points.addListener(this::handleListChange);
    }

    private void handleListChange(ListChangeListener.Change<? extends PointLayerNode> c) {
        AnchorPane r = getRoot();

        List<Circle> removed = c.getRemoved()
                .stream()
                .peek(n -> {
                    r.widthProperty()
                            .removeListener(n.getResizeChangeListener());
                    r.heightProperty()
                            .removeListener(n.getResizeChangeListener());
                })
                .map(PointLayerNode::getShape)
                .collect(Collectors.toList());
        r.getChildren().removeAll(removed);

        List<Circle> added = c.getAddedSubList()
                .stream()
                .peek(n -> {
                    r.widthProperty()
                            .addListener(n.getResizeChangeListener());
                    r.heightProperty()
                            .addListener(n.getResizeChangeListener());
                })
                .map(PointLayerNode::getShape)
                .collect(Collectors.toList());
        r.getChildren().addAll(added);
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
                .peek(n -> n.getShape().setFill(color))
                .collect(Collectors.toList());
        points.addAll(layerNodes);
    }

    @Override
    public void clear() {
        JFXUtilities.runOnFXThread(points::clear);
    }

    @Override
    public ToolBar getToolBar() {
        if (toolBar == null) {
            toolBar = new ToolBar();
            // Add Concept combobox
            toolBar.getItems().add(new JFXComboBox<String>());

        }
        return toolBar;
    }

    @Override
    public void setDisable(boolean disable) {

        if (disable) {
            // Remove shapes
            List<Circle> circles = points.stream()
                    .map(PointLayerNode::getShape)
                    .collect(Collectors.toList());
            getRoot().getChildren().removeAll(circles);
            stackPane.getChildren().remove(getRoot());
        }
        else if (this.disable) {
            // If we toggle from disabled to enabled then add shapes
            List<Circle> circles = points.stream()
                    .map(PointLayerNode::getShape)
                    .collect(Collectors.toList());
            getRoot().getChildren().addAll(circles);
            stackPane.getChildren().add(getRoot());
        }

    }

    @Override
    public boolean isDisabled() {
        return disable;
    }


    @Override
    public Text getToggleGraphic() {
        MaterialIconFactory iconFactory = MaterialIconFactory.get();
        return iconFactory.createIcon(MaterialIcon.CONTROL_POINT, "30px");
    }
}
