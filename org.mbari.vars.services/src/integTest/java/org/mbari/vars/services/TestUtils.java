package org.mbari.vars.services;

import org.mbari.vars.core.util.InstantUtils;
import org.mbari.vars.core.util.StringUtils;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.services.model.ImageReference;
import org.mbari.vars.services.model.Media;
import org.mbari.vcr4j.time.FrameRates;
import org.mbari.vcr4j.time.Timecode;

import java.net.MalformedURLException;
import java.net.URI;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

public class TestUtils {

    private static final Random random = new Random();


    public static Media buildRandomMedia() {

        try {
            var digest = MessageDigest.getInstance("SHA-512");
            var cameraId = StringUtils.random(10);
            var diveNumber = random.nextInt(9999);
            var startTime = Instant.now();
            var duration = Duration.ofSeconds(random.nextInt(3600));
            var videoSequenceName = cameraId + "-" + diveNumber;
            var videoName = videoSequenceName + "_" + InstantUtils.COMPACT_TIME_FORMATTER.format(startTime);
            var uri = URI
                    .create("http://www.foo.bar/" + cameraId + "/" + cameraId.charAt(0) + "_" + diveNumber + ".mp4");

            var m = new Media();
            m.setCameraId(cameraId);
            m.setVideoSequenceName(videoSequenceName);
            m.setVideoName(videoName);
            m.setUri(uri);
            m.setStartTimestamp(startTime);
            m.setDuration(duration);
            m.setDescription(StringUtils.random(100));
            m.setSha512(digest.digest(videoName.getBytes()));
            m.setVideoReferenceUuid(UUID.randomUUID());
            m.setVideoUuid(UUID.randomUUID());
            m.setVideoReferenceUuid(UUID.randomUUID());
            return m;
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to create random media", e);
        }
    }

    public static Annotation buildRandomAnnotation() {
        var a = new Annotation();
        a.setConcept(StringUtils.random(30));
        a.setObserver(StringUtils.random(10));
        a.setObservationTimestamp(Instant.now());
        a.setVideoReferenceUuid(UUID.randomUUID());
        if (random.nextBoolean()) {
            var t =  random.nextDouble(1000);
            var tc = new Timecode(t, FrameRates.NTSC);
            a.setTimecode(tc);
        }
        if (random.nextBoolean()) {
            a.setElapsedTime(Duration.ofSeconds(random.nextInt(60)));
        }
        a.setRecordedTimestamp(Instant.now());
        if(random.nextBoolean()) {
            a.setDuration(Duration.ofSeconds(random.nextInt(60)));
        }
        a.setGroup(StringUtils.random(10));
        a.setActivity(StringUtils.random(10));

        return a;
    }

    public static Association buildRandomAssociation() {
        var linkName = StringUtils.random(10);
        var linkValue = StringUtils.random(255);
        var toConcept = StringUtils.random(10);
        var mimeType = random.nextBoolean() ? "text/plain" : "application/json";
        return new Association(linkName, toConcept, linkValue, mimeType);
    }

    public static ImageReference buildRandomImageReference() {
        var ir = new ImageReference();
        try {
            var url =URI.create("http://www.foo.bar/" + StringUtils.random(30) + ".jpg").toURL();
            ir.setUrl(url);
        } catch (MalformedURLException e) {
            // this shouldn't ever happen
            throw new RuntimeException(e);
        }
        ir.setDescription(StringUtils.random(100));
        ir.setFormat("image/jpeg");
        ir.setWidth(random.nextInt(1920));
        ir.setHeight(random.nextInt(1080));
        return ir;
    }

    public static Annotation buildRandomAnnotation(Media media) {
        var a = buildRandomAnnotation();
        a.setVideoReferenceUuid(media.getVideoReferenceUuid());
        var elapsedTime = Duration.ofSeconds(random.nextInt((int) media.getDuration().getSeconds()));
        var recordedTimestamp = media.getStartTimestamp().plus(elapsedTime);
        a.setRecordedTimestamp(recordedTimestamp);
        a.setElapsedTime(elapsedTime);
        return a;
    }
}
