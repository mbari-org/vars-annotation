package org.mbari.vars.annotation.test.services.model;


import org.junit.jupiter.api.Test;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annotation.services.annosaurus.Annotations;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vcr4j.VideoIndex;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MediaTest {

    @Test
    public void testToMediaElaspsedTime1() {
        var timestamp = Instant.parse("2000-01-01T00:00:00Z");
        var elapsedTime = Duration.ofMillis(1000);
        var recordedTimestamp = timestamp.plus(elapsedTime);

        var media = new Media();
        media.setVideoReferenceUuid(UUID.randomUUID());
        media.setStartTimestamp(timestamp);
        var a0 = new Annotation("Media UUID matches", "brian", new VideoIndex(elapsedTime), media.getVideoReferenceUuid());
        var a1 = new Annotation("Media UUID does not match", "brian", new VideoIndex(Duration.ZERO, recordedTimestamp), UUID.randomUUID());

        // Same videoReferenceUUID should return annos elapsedTime as is
        var opt1 = Annotations.toMediaElapsedTime(media, a0);
        assertTrue(opt1.isPresent());
        assertEquals(elapsedTime, opt1.get());

        // Different videoReferenceUuid should munge the annos elapsedTime relative to the media using
        // the annos recordedTimestamp. Annos elapsedTime = 9, but it'sr ecorded date is 1 seconds from
        // the start of the video
        var opt2 = Annotations.toMediaElapsedTime(media, a1);
        assertTrue(opt2.isPresent());
        assertEquals(elapsedTime, opt2.get());

    }
}
