package org.mbari.vars.annotation.ui.mediaplayers.ships;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * @author Brian Schlining
 * @since 2017-12-21T10:01:00
 */
public class MediaParams {
    private final Long sequenceNumber;
    private final String cameraId;
    private final String videoSequenceName;
    private final Instant startTimestamp;
    private final String videoName;
    private final URI uri;
    private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    public static final String URI_PREFIX = "urn:rtva:org.mbari:";

    public MediaParams(String cameraId, Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        this.cameraId = cameraId;

        videoSequenceName = String.format("%s %04d", cameraId, sequenceNumber);
        startTimestamp = Instant.now();
        videoName = videoSequenceName + " real-time";
                //df.format(Instant.now().atZone(ZoneId.of("UTC")));
        String uriName = videoSequenceName.replaceAll("\\s+", "_");
        try {
            uri = new URI(URI_PREFIX + uriName);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    public String getCameraId() {
        return cameraId;
    }

    public String getVideoSequenceName() {
        return videoSequenceName;
    }

    public Instant getStartTimestamp() {
        return startTimestamp;
    }

    public String getVideoName() {
        return videoName;
    }

    public URI getUri() {
        return uri;
    }

}
