package org.mbari.vars.annotation.ui.javafx.buttons;

import io.reactivex.rxjava3.core.Observable;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;
import org.mbari.vars.annosaurus.sdk.r1.models.BoundingBox;
import org.mbari.vars.annotation.etc.gson.Gsons;
import org.mbari.vars.annotation.services.annosaurus.BoundingBoxes;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.commands.CreateAssociationsCmd;
import org.mbari.vars.annotation.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.annotation.ui.javafx.Icons;
import org.mbari.vars.annotation.ui.mediaplayers.AnnotationPlacement;
import org.mbari.vars.annotation.ui.util.JFXUtilities;
import org.mbari.vars.oni.sdk.r1.models.User;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Button Controller that adds a localization (a bounding box association) to the selected
 * annotation. The box is centered in the video and is 10% of the video's width and height.
 */
public class AddLocalizationBC extends AbstractBC {

    /** Size of the box as a fraction of the video's width and height */
    private static final double BOX_FRACTION = 0.1;

    /** Incremented for each enable check so that results from older checks can be ignored */
    private final AtomicLong enableCheckCount = new AtomicLong();

    public AddLocalizationBC(Button button, UIToolBox toolBox) {
        super(button, toolBox);
    }

    @Override
    protected void init() {
        String tooltip = toolBox.getI18nBundle().getString("buttons.localization");
        Text icon = Icons.CROP_FREE.standardSize();
        initializeButton(tooltip, icon);

        Observable<Object> observable = toolBox.getEventBus().toObserverable();
        observable.ofType(AnnotationsSelectedEvent.class)
                .subscribe(m -> checkEnable(m.get()));
    }

    private void checkEnable(Collection<Annotation> selectedAnnotations) {
        // Snapshot the selection; the check below runs on another thread
        List<Annotation> selected = selectedAnnotations == null ? List.of() : List.copyOf(selectedAnnotations);
        long check = enableCheckCount.incrementAndGet();
        User user = toolBox.getData().getUser();
        boolean maybeEnable = user != null &&
                hasVideoSize(toolBox.getData().getMedia()) &&
                selected.size() == 1;
        if (!maybeEnable) {
            JFXUtilities.runOnFXThread(() -> button.setDisable(true));
            return;
        }
        // The box is only useful if the annotation can be shown in the current video. Finding out
        // can require a media lookup, so keep it off the UI threads.
        toolBox.getExecutorService().submit(() -> {
            boolean enable = AnnotationPlacement.elapsedTimeInCurrentMedia(toolBox, selected.getFirst()).isPresent();
            JFXUtilities.runOnFXThread(() -> {
                if (check == enableCheckCount.get()) { // ignore results for an older selection
                    button.setDisable(!enable);
                }
            });
        });
    }

    @Override
    protected void checkEnable() {
        checkEnable(toolBox.getData().getSelectedAnnotations());
    }

    @Override
    protected void apply() {
        Media media = toolBox.getData().getMedia();
        List<Annotation> selected = new ArrayList<>(toolBox.getData().getSelectedAnnotations());
        if (selected.size() == 1 && hasVideoSize(media)) {
            Association association = centeredBoundingBox(media.getWidth(), media.getHeight());
            toolBox.getEventBus().send(new CreateAssociationsCmd(association, selected));
        }
    }

    private static boolean hasVideoSize(Media media) {
        return media != null &&
                media.getWidth() != null && media.getWidth() > 0 &&
                media.getHeight() != null && media.getHeight() > 0;
    }

    /**
     * @param videoWidth The video's width in pixels
     * @param videoHeight The video's height in pixels
     * @return A bounding box association centered in the video
     */
    public static Association centeredBoundingBox(int videoWidth, int videoHeight) {
        int width = Math.max(1, (int) Math.round(videoWidth * BOX_FRACTION));
        int height = Math.max(1, (int) Math.round(videoHeight * BOX_FRACTION));
        int x = (videoWidth - width) / 2;
        int y = (videoHeight - height) / 2;
        BoundingBox box = new BoundingBox(x, y, width, height, BoundingBoxes.GENERATOR);
        String json = Gsons.SNAKE_CASE_GSON.toJson(box);
        return new Association(BoundingBox.LINK_NAME, Association.VALUE_SELF, json, "application/json");
    }
}
