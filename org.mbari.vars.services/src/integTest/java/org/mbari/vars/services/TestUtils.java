package org.mbari.vars.services;

import org.mbari.vars.core.util.InstantUtils;
import org.mbari.vars.core.util.StringUtils;
import org.mbari.vars.services.model.Media;

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
}
