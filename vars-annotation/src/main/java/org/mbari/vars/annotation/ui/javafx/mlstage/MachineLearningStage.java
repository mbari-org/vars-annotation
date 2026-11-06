package org.mbari.vars.annotation.ui.javafx.mlstage;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.mbari.imgfx.imageview.ImagePaneController;
import org.mbari.imgfx.roi.Localization;
import org.mbari.imgfx.roi.RectangleView;
import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.javafx.Icons;
import org.mbari.vars.annotation.ui.javafx.imagestage.ImageStage2;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MachineLearningStage extends Stage {

    private ImagePaneController imagePaneController;
    private final List<Localization<RectangleView, ImageView>> localizations = new CopyOnWriteArrayList<>();
    private static final Loggers log = new Loggers(ImageStage2.class);
    private VBox rightPane = new VBox();
    private final UIToolBox toolBox;
    private Button saveAnnotationsButton;
    private Button saveAllButton;
    private Button cancelButton;
    private final VBox loadingPane = new VBox();
    private final List<LocalizationEditorController> editors = new ArrayList<>();

    /** Opacity of the boxes that are not associated with the hovered label */
    private static final double DIMMED_OPACITY = 0.15;

    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;
    /** Matches -fx-background in annotation.css */
    static final Color BACKGROUND = Color.valueOf("#2b3940");

    public MachineLearningStage(UIToolBox toolBox) {
        this.toolBox = toolBox;
        init();
    }

    private void init() {
        rightPane.setPadding(new Insets(15));

        var imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);

        imagePaneController = new ImagePaneController(imageView);
        var pane = imagePaneController.getPane();

        // Shown while waiting on the frame capture and the remote ML service
        var progressIndicator = new ProgressIndicator();
        var analyzingLabel = new Label(toolBox.getI18nBundle().getString("ml.stage.analyzing"));
        loadingPane.getChildren().addAll(progressIndicator, analyzingLabel);
        loadingPane.setAlignment(Pos.CENTER);
        loadingPane.setSpacing(10);
        loadingPane.setVisible(false);

        // Do NOT bind the image pane's pref size to the root's size. The root's
        // width includes the right pane, so that feeds back into the root's pref
        // size; JavaFX re-sizes an unsized stage to that pref on every show(), so
        // the window grew on every use until it was too large to render. The
        // BorderPane already resizes its center to fill the available space.
        BorderPane root = new BorderPane(new StackPane(pane, loadingPane));

        var scrollPane = new ScrollPane();
        scrollPane.setContent(rightPane);
        root.setRight(scrollPane);

        var expander = new Region();
        HBox.setHgrow(expander, Priority.ALWAYS);
        var saveAnnotationsIcon = Icons.SAVE.standardSize();

        saveAnnotationsButton = new Button(null, saveAnnotationsIcon);


        var saveAllIcon = Icons.FILE_UPLOAD.standardSize();
        saveAllButton = new Button(null, saveAllIcon);

        var cancelIcon = Icons.CANCEL.standardSize();
        cancelButton = new Button(null, cancelIcon);
        var hbox = new HBox(expander, cancelButton, saveAnnotationsButton, saveAllButton);

        root.setBottom(hbox);

        // An explicit scene size keeps the window from being sized to the full
        // resolution of the video frame. JavaFX keeps the scene's current size
        // (including user resizes) when the stage is shown again.
        var screenBounds = Screen.getPrimary().getVisualBounds();
        var width = Math.min(DEFAULT_WIDTH, screenBounds.getWidth() * 0.9);
        var height = Math.min(DEFAULT_HEIGHT, screenBounds.getHeight() * 0.9);
        Scene scene = new Scene(root, width, height);
        // The default fill is white, which flashes before the first frame is rendered
        scene.setFill(BACKGROUND);
        scene.getStylesheets()
                .addAll(toolBox.getStylesheets());
        setScene(scene);
    }

    /**
     * Shows the stage immediately with a progress indicator while the ML analysis runs.
     * Any previous image and localizations are cleared.
     */
    public void showLoading() {
        setLocalizations(List.of());
        setImage(null);
        setButtonsDisabled(true);
        loadingPane.setVisible(true);
        if (!isShowing()) {
            show();
        }
        toFront();
    }

    /**
     * Hides the progress indicator and enables the buttons. Call after the image and
     * localizations have been set.
     */
    public void showResults() {
        loadingPane.setVisible(false);
        setButtonsDisabled(false);
    }

    private void setButtonsDisabled(boolean disabled) {
        saveAnnotationsButton.setDisable(disabled);
        saveAllButton.setDisable(disabled);
    }


    public void setImage(Image image) {
        imagePaneController.getView().setImage(image);
    }

    public BorderPane getRoot() {
        return (BorderPane) getScene().getRoot();
    }

    public ImagePaneController getImagePaneController() {
        return imagePaneController;
    }

    public Button getSaveAnnotationsButton() {
        return saveAnnotationsButton;
    }

    public Button getSaveAllButton() {
        return saveAllButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }

    public synchronized void setLocalizations(Collection<Localization<RectangleView, ImageView>> locs) {
        rightPane.getChildren().clear();
        localizations.forEach(loc -> {
            setDimmed(loc, false);
            loc.setVisible(false);
        });
        localizations.clear();
        editors.clear();
        localizations.addAll(locs);
        localizations.sort(Comparator.comparing(Localization::getLabel));
        localizations.forEach(loc -> {
            var editor = new LocalizationEditorController(toolBox, loc);
            editors.add(editor);
            rightPane.getChildren().add(editor.getRoot());
            // Color by concept, and recolor if the user picks a different concept
            MLUtil.applyConceptColor(loc);
            loc.labelProperty().addListener((obs, oldv, newv) -> MLUtil.applyConceptColor(loc));
            // Dim the other boxes while this label is hovered so the user can see which box they'll edit
            editor.highlightedProperty().addListener((obs, oldv, newv) -> updateHighlight());
            loc.setVisible(true);
        });
    }

    /**
     * Dims every box except the one whose label in the side bar is highlighted. If no
     * label is highlighted, all boxes are shown normally.
     */
    private void updateHighlight() {
        // Prefer the editor under the mouse, then one whose concept dropdown is open
        var active = editors.stream()
                .filter(LocalizationEditorController::isHovered)
                .findFirst()
                .or(() -> editors.stream().filter(e -> e.highlightedProperty().get()).findFirst())
                .map(LocalizationEditorController::getLocalization)
                .orElse(null);
        localizations.forEach(loc -> setDimmed(loc, active != null && loc != active));
    }

    private void setDimmed(Localization<RectangleView, ImageView> localization, boolean dimmed) {
        var opacity = dimmed ? DIMMED_OPACITY : 1.0;
        var rectangle = localization.getDataView().getView();
        rectangle.setOpacity(opacity);
        // imgfx doesn't expose a Localization's text label. It adds the label to the
        // pane immediately after the rectangle (in the same addAll call), so it is
        // the rectangle's next sibling.
        var children = imagePaneController.getPane().getChildren();
        var idx = children.indexOf(rectangle);
        if (idx >= 0 && idx + 1 < children.size() && children.get(idx + 1) instanceof Text label) {
            label.setOpacity(opacity);
        }
    }

    public List<Localization<RectangleView, ImageView>> getLocalizations() {
        return List.copyOf(localizations);
    }
}
