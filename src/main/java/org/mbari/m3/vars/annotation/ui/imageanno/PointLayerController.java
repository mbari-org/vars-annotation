package org.mbari.m3.vars.annotation.ui.imageanno;

import com.jfoenix.controls.JFXComboBox;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.ui.shared.FilteredComboBoxDecorator;
import org.mbari.m3.vars.annotation.ui.shared.ImageViewExt;
import org.mbari.m3.vars.annotation.util.JFXUtilities;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PointLayerController extends AbstractLayerController {

    private boolean disable = true;
    private ToolBar toolBar;
    private ComboBox<String> conceptComboBox;
    private final String LINK_NAME;
    private final ObservableList<PointLayerNode> points = FXCollections.observableArrayList();
    private EventHandler<? super MouseEvent> eventHandler = this::handleMouseEvent;

    public PointLayerController(Data data, UIToolBox toolBox) {
        super(data);
        LINK_NAME = toolBox.getConfig().getString("app.annotation.image.point.linkname");        // Bind size tto the pane that contains this anchor pane

        points.addListener(this::handleListChange);
    }

    @Override
    public Node getEnableButtonGraphic() {
        MaterialIconFactory iconFactory = MaterialIconFactory.get();
        return iconFactory.createIcon(MaterialIcon.CONTROL_POINT, "30px");
    }

    public ComboBox<String> getConceptComboBox() {
        if (conceptComboBox == null) {
            conceptComboBox = new JFXComboBox<>();
            new FilteredComboBoxDecorator<>(conceptComboBox,
                    FilteredComboBoxDecorator.STARTSWITH_FIRST_THEN_CONTAINS_CHARS_IN_ORDER);
        }
        return conceptComboBox;
    }

    private void handleMouseEvent(MouseEvent event) {
        Point2D point = LayerController.toImageCoordinates(event, getData().getImageViewExt());
        System.out.println(point);
    }

    private void handleListChange(ListChangeListener.Change<? extends PointLayerNode> c) {
        AnchorPane r = getRoot();

        while (c.next()) {
            if (c.wasRemoved()) {
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
            }
            if (c.wasAdded()) {
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
        }
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
            toolBar.getItems().add(getConceptComboBox());

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
            getData().getStackPane().getChildren().remove(getRoot());
            getRoot().removeEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);
        }
        else if (this.disable) {
            // If we toggle from disabled to enabled then add shapes
            List<Circle> circles = points.stream()
                    .map(PointLayerNode::getShape)
                    .collect(Collectors.toList());
            getRoot().getChildren().addAll(circles);
            getData().getStackPane().getChildren().add(getRoot());
            getRoot().addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);
        }

    }

    @Override
    public boolean isDisabled() {
        return disable;
    }




//    @Override
//    public void postRegister(ImageAnnotationPaneController controller) {
//        getRoot().addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
//            Point2D point = LayerController.toImageCoordinates(event, controller.getImageViewExt());
//            System.out.println(point);
//            String linkValue =
//            new Association(LINK_NAME,
//                    Association.VALUE_SELF,
//                    )
//        });
//    }
}
