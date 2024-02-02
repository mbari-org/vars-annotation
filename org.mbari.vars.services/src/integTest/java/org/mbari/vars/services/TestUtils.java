package org.mbari.vars.services;

import org.mbari.vars.core.util.InstantUtils;
import org.mbari.vars.core.util.StringUtils;
import org.mbari.vars.services.model.*;
import org.mbari.vcr4j.time.FrameRates;
import org.mbari.vcr4j.time.Timecode;

import java.net.MalformedURLException;
import java.net.URI;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

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

    public static List<Annotation> buildRandomAnnotations(int count, boolean extend) {
        var xs = TestUtils.buildRandomAnnotations(count);
        if (extend) {
            xs.forEach(y -> {

                // Add association
                var ass = TestUtils.buildRandomAssociation();
                y.setAssociations(List.of(ass));

                // Add data
                var d = TestUtils.buildRandomAncillaryData();
                d.setImagedMomentUuid(y.getImagedMomentUuid());
                y.setAncillaryData(d);

                // add image reference
                var i = TestUtils.buildRandomImageReference();
                var j = new Image(y, i);
                var ir = new ImageReference(j);
                y.setImageReferences(List.of(ir));
            });
        }
        return new ArrayList<>(xs);
    }

    public static List<Annotation> buildRandomAnnotations(int count) {
        var videoReferenceUuid = UUID.randomUUID();
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    var a = buildRandomAnnotation();
                    a.setVideoReferenceUuid(videoReferenceUuid);
                    return a;
                })
                .toList();
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
        if (random.nextBoolean()) {
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

    public static AncillaryData buildRandomAncillaryData() {
        var ad = new AncillaryData();
        ad.setRecordedTimestamp(Instant.now());
        ad.setAltitude(random.nextDouble(1000));
        ad.setCrs(StringUtils.random(10));
        ad.setDepthMeters(random.nextDouble(4000));
        ad.setLatitude(random.nextDouble(90));
        ad.setLightTransmission(random.nextDouble(100));
        ad.setLongitude(random.nextDouble(180));
        ad.setOxygenMlL(random.nextDouble(10));
        ad.setPhi(random.nextDouble(360));
        ad.setPosePositionUnits(StringUtils.random(10));
        ad.setPressureDbar(random.nextDouble(4000));
        ad.setPsi(random.nextDouble(360));
        ad.setSalinity(random.nextDouble(40));
        ad.setTemperatureCelsius(random.nextDouble(15));
        ad.setTheta(random.nextDouble(360));
        ad.setX(random.nextDouble(1000));
        ad.setY(random.nextDouble(1000));
        ad.setZ(random.nextDouble(1000));
        return ad;
    }

    public static CachedVideoReference buildRandomCachedVideoReference() {
        return new CachedVideoReference(StringUtils.random(32), StringUtils.random(32),
                UUID.randomUUID(), StringUtils.random(64), UUID.randomUUID());

    }

    public static Timecode randomTimecode() {
        var t =  random.nextDouble(3600);
        var ntsc = new Timecode(t, FrameRates.NTSC);
        return new Timecode(ntsc.toString());
    }
}
