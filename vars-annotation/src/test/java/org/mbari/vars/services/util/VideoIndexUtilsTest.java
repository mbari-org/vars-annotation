package org.mbari.vars.services.util;

import org.junit.Test;
import org.mbari.vars.services.model.Media;
import org.mbari.vcr4j.VideoIndex;

import java.time.Duration;
import java.time.Instant;

import static org.junit.Assert.*;
public class VideoIndexUtilsTest {

    private Instant now = Instant.now();
    private Media media1 = newMedia(now);
    private Media media2 = newMedia(now.plus(Duration.ofMinutes(1)));

    @Test
    public void resolveAddElapsedTimeTest() {
        var et = Duration.ofMinutes(2);
        var ts = now.plus(et);
        var vi = new VideoIndex(ts);
        var rvi = VideoIndexUtils.resolve(vi, media1);
        assertTrue(rvi.getTimestamp().isPresent());
        assertEquals(ts, rvi.getTimestamp().get());
        assertTrue(rvi.getElapsedTime().isPresent());
        assertEquals(et, rvi.getElapsedTime().get());
    }

    @Test
    public void resolveAddTimestampTest() {
        var et = Duration.ofMinutes(2);
        var ts = now.plus(et);
        var vi = new VideoIndex(et);
        var rvi = VideoIndexUtils.resolve(vi, media1);
        assertTrue(rvi.getTimestamp().isPresent());
        assertEquals(ts, rvi.getTimestamp().get());
        assertTrue(rvi.getElapsedTime().isPresent());
        assertEquals(et, rvi.getElapsedTime().get());
    }


    private Media newMedia(Instant instant) {
        var media = new Media();
        media.setStartTimestamp(instant);
        media.setDuration(Duration.ofMinutes(10));
        return media;
    }
}
