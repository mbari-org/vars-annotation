package org.mbari.vars.services;

import org.mbari.vars.services.model.*;
import org.mbari.vcr4j.time.Timecode;

import java.util.Optional;

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

    public static void assertSameAnnotation(Annotation a, Annotation b, boolean checkUuid, boolean checkRelations) {
        if (checkUuid) {
            assertEquals(a.getObservationUuid(), b.getObservationUuid());
            assertEquals(a.getImagedMomentUuid(), b.getImagedMomentUuid());
        }
        assertEquals(a.getVideoReferenceUuid(), b.getVideoReferenceUuid());
        assertEquals(a.getConcept(), b.getConcept());
        assertEquals(a.getObserver(), b.getObserver());
        assertEquals(a.getObservationTimestamp(), b.getObservationTimestamp());
        assertEquals(a.getVideoReferenceUuid(), b.getVideoReferenceUuid());
        assertEquals(a.getDuration(), b.getDuration());
        assertEquals(a.getElapsedTime(), b.getElapsedTime());
        assertEquals(a.getRecordedTimestamp(), b.getRecordedTimestamp());
        assertEquals(a.getGroup(), b.getGroup());
        assertEquals(a.getActivity(), b.getActivity());
        if (checkRelations) {
            assertEquals(a.getAssociations().size(), b.getAssociations().size());

            assertEquals(a.getImageReferences().size(), b.getImageReferences().size());
        }
    }

    public static void assertSameAssociation(Association a, Association b, boolean checkUuid) {
        assertEquals(a.getLinkName(), b.getLinkName());
        assertEquals(a.getLinkValue(), b.getLinkValue());
        assertEquals(a.getToConcept(), b.getToConcept());
        assertEquals(a.getMimeType(), b.getMimeType());
        if (checkUuid) {
            assertEquals(a.getUuid(), b.getUuid());
        }
    }

    public static void assertSameImageReference(ImageReference a, ImageReference b, boolean checkUuid) {
        assertEquals(a.getFormat(), b.getFormat());
        assertEquals(a.getUrl(), b.getUrl());
        assertEquals(a.getDescription(), b.getDescription());
        assertEquals(a.getWidth(), b.getWidth());
        assertEquals(a.getHeight(), b.getHeight());
        if (checkUuid) {
            assertEquals(a.getUuid(), b.getUuid());
        }
    }

    public static void assertSameImage(Image a, Image b, boolean checkUuid) {
        assertEquals(a.getFormat(), b.getFormat());
        assertEquals(a.getUrl(), b.getUrl());
        assertEquals(a.getDescription(), b.getDescription());
        assertEquals(a.getWidth(), b.getWidth());
        assertEquals(a.getHeight(), b.getHeight());
        var tca = Optional.ofNullable(a.getTimecode()).orElse(Timecode.zero()).toString();
        var tcb = Optional.ofNullable(a.getTimecode()).orElse(Timecode.zero()).toString();
        assertEquals(tca, tcb);
        assertEquals(a.getElapsedTime(), b.getElapsedTime());
        assertEquals(a.getRecordedTimestamp(), b.getRecordedTimestamp());
        assertEquals(a.getVideoReferenceUuid(), b.getVideoReferenceUuid());
        if (checkUuid) {
            assertEquals(a.getImageReferenceUuid(), b.getImageReferenceUuid());
            assertEquals(a.getImagedMomentUuid(), b.getImagedMomentUuid());
        }
    }
}
