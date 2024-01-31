package org.mbari.vars.services;

import org.mbari.vars.services.model.Media;
import static org.junit.Assert.*;

public class AssertUtils {

    public static void assertSameMedia(Media a, Media b, boolean checkUuid) {
        if (checkUuid) {
            assertEquals(a.getVideoReferenceUuid(), b.getVideoReferenceUuid());
            assertEquals(a.getVideoUuid(), b.getVideoUuid());
            assertEquals(a.getVideoSequenceUuid(), b.getVideoSequenceUuid());
        }
        assertEquals(a.getAudioCodec(), b.getAudioCodec());
        assertEquals(a.getCameraId(), b.getCameraId());
        assertEquals(a.getContainer(), b.getContainer());
        assertEquals(a.getDuration(), b.getDuration());
        assertArrayEquals(a.getSha512(), b.getSha512());
        assertEquals(a.getUri(), b.getUri());
        assertEquals(a.getVideoSequenceName(), b.getVideoSequenceName());
        assertEquals(a.getVideoName(), b.getVideoName());
        assertEquals(a.getVideoCodec(), b.getVideoCodec());
        assertEquals(a.getStartTimestamp(), b.getStartTimestamp());
        assertEquals(a.getFrameRate(), b.getFrameRate());
        assertEquals(a.getWidth(), b.getWidth());
        assertEquals(a.getHeight(), b.getHeight());
        assertEquals(a.getSizeBytes(), b.getSizeBytes());
        assertEquals(a.getDescription(), b.getDescription());
        assertEquals(a.getVideoDescription(), b.getVideoDescription());
        assertEquals(a.getVideoSequenceDescription(), b.getVideoSequenceDescription());
    }
}
