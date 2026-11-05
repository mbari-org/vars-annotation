package org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2;

import javafx.application.Platform;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annotation.etc.jdk.Loggers;
import org.mbari.vars.annotation.ui.UIToolBox;

import java.util.List;
import java.util.function.Supplier;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Data.annotations is a JavaFX ObservableList owned by the FX application thread. The
 * sharktopoda2 controllers run on other threads (PlayerIO's UDP receive thread, the
 * toolbox executor) and must not iterate it there: a concurrent mutation throws
 * ConcurrentModificationException inside the Rx subscriber, which cancels the
 * subscription and silently kills localization handling until the media is reopened.
 * This helper copies the list on the FX thread.
 */
class AnnotationSnapshots {

    private static final Loggers log = new Loggers(AnnotationSnapshots.class);

    private AnnotationSnapshots() {
        // static helper
    }

    static List<Annotation> snapshot(UIToolBox toolBox) {
        var data = toolBox.getData();
        return copyOnFxThread(data::getAnnotations);
    }

    /** @return A copy of the annotations currently selected in VARS (also FX-thread owned) */
    static List<Annotation> selected(UIToolBox toolBox) {
        var data = toolBox.getData();
        return copyOnFxThread(data::getSelectedAnnotations);
    }

    /** @param list Fetches the list. Only invoked on the FX thread, so Data is never touched off it */
    private static List<Annotation> copyOnFxThread(Supplier<List<Annotation>> list) {
        if (Platform.isFxApplicationThread()) {
            return List.copyOf(list.get());
        }
        var future = new CompletableFuture<List<Annotation>>();
        Platform.runLater(() -> {
            try {
                future.complete(List.copyOf(list.get()));
            }
            catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        try {
            return future.get(5, TimeUnit.SECONDS);
        }
        catch (Exception e) {
            log.atWarn().withCause(e).log("Failed to snapshot the annotations on the FX thread");
            return List.of();
        }
    }
}
