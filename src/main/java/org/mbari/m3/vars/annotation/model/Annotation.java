package org.mbari.m3.vars.annotation.model;

import org.mbari.vcr4j.time.Timecode;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
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

    public UUID getObservationUuid() {
        return observationUuid;
    }

    public void setObservationUuid(UUID observationUuid) {
        this.observationUuid = observationUuid;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getObserver() {
        return observer;
    }

    public void setObserver(String observer) {
        this.observer = observer;
    }

    public Instant getObservationTimestamp() {
        return observationTimestamp;
    }

    public void setObservationTimestamp(Instant observationTimestamp) {
        this.observationTimestamp = observationTimestamp;
    }

    public UUID getVideoReferenceUuid() {
        return videoReferenceUuid;
    }

    public void setVideoReferenceUuid(UUID videoReferenceUuid) {
        this.videoReferenceUuid = videoReferenceUuid;
    }

    public UUID getImagedMomentUuid() {
        return imagedMomentUuid;
    }

    public void setImagedMomentUuid(UUID imagedMomentUuid) {
        this.imagedMomentUuid = imagedMomentUuid;
    }

    public Timecode getTimecode() {
        return timecode;
    }

    public void setTimecode(Timecode timecode) {
        this.timecode = timecode;
    }

    public Duration getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(Duration elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public Instant getRecordedTimestamp() {
        return recordedTimestamp;
    }

    public void setRecordedTimestamp(Instant recordedTimestamp) {
        this.recordedTimestamp = recordedTimestamp;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public List<Association> getAssociations() {
        return associations;
    }

    public void setAssociations(List<Association> associations) {
        this.associations = Collections.unmodifiableList(associations);
    }

    public List<ImageReference> getImages() {
        return images;
    }

    public void setImages(List<ImageReference> images) {
        this.images = images;
    }
}
