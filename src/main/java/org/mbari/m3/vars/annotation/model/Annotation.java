package org.mbari.m3.vars.annotation.model;

import org.mbari.vcr4j.time.Timecode;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-05-11T13:26:00
 */
public class Annotation {

    private UUID observationUuid;
    private String concept;
    private String observer;
    private Instant observationTimestamp;
    private UUID videoReferenceUuid;
    private UUID imagedMomentUuid;
    private Timecode timecode;
    private Duration elapsedTime;
    private Instant recordedTimestamp;
    private Duration duration;
    private String group;
    private String activity;
    private List<Association> associations;
    private List<ImageReference> images;
}
