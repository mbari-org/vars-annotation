package org.mbari.m3.vars.annotation.model;

import org.mbari.vcr4j.time.Timecode;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-05-11T13:30:00
 */
public class Image {
    private UUID imageReferenceUuid;
    private String format;
    private Integer width;
    private Integer height;
    private URL url;
    private String description;
    private UUID videoReferenceUuid;
    private UUID imagedMomentUuid;
    private Timecode timecode;
    private Duration elapsedTime;
    private Instant recordedTimestamp;

}
