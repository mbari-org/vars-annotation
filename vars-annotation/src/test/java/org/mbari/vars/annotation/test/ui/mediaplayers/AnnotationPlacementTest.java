package org.mbari.vars.annotation.test.ui.mediaplayers;

import org.junit.jupiter.api.Test;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annotation.ui.mediaplayers.AnnotationPlacement;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnnotationPlacementTest {

    private static final Instant START = Instant.parse("2021-05-22T19:08:04Z");

    private static Media media(Duration duration) {
        var m = new Media();
        m.setStartTimestamp(START);
        m.setDuration(duration);
        return m;
    }

    private static Annotation annotation(Duration elapsedTime, Instant recordedTimestamp) {
        var a = new Annotation();
        a.setElapsedTime(elapsedTime);
        a.setRecordedTimestamp(recordedTimestamp);
        return a;
    }

    @Test
    public void usesElapsedTimeWhenPresent() {
        var a = annotation(Duration.ofSeconds(10), START.plusSeconds(99));
        assertEquals(Optional.of(Duration.ofSeconds(10)), AnnotationPlacement.elapsedTime(a, media(null)));
    }

    @Test
    public void derivesElapsedTimeFromRecordedTimestamp() {
        var a = annotation(null, START.plusMillis(61_500));
        assertEquals(Optional.of(Duration.ofMillis(61_500)), AnnotationPlacement.elapsedTime(a, media(Duration.ofMinutes(10))));
    }

    @Test
    public void emptyWhenRecordedTimestampIsOutsideTheMedia() {
        var before = annotation(null, START.minusSeconds(1));
        assertEquals(Optional.empty(), AnnotationPlacement.elapsedTime(before, media(Duration.ofMinutes(10))));
        var after = annotation(null, START.plus(Duration.ofMinutes(11)));
        assertEquals(Optional.empty(), AnnotationPlacement.elapsedTime(after, media(Duration.ofMinutes(10))));
    }

    @Test
    public void emptyWhenThereIsNothingToWorkFrom() {
        assertEquals(Optional.empty(), AnnotationPlacement.elapsedTime(annotation(null, null), media(null)));
        var noStart = new Media();
        assertEquals(Optional.empty(), AnnotationPlacement.elapsedTime(annotation(null, START), noStart));
    }
}
