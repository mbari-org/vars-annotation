package org.mbari.m3.vars.annotation.model;

import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.time.Timecode;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-05-11T13:30:00
 */
public class Image implements ImagedMoment, Cloneable {
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

    public Image() {
    }

    public Image(Annotation annotation, ImageReference imageReference) {
        format = imageReference.getFormat();
        width = imageReference.getWidth();
        height = imageReference.getHeight();
        url = imageReference.getUrl();
        description = imageReference.getDescription();
        videoReferenceUuid = annotation.getVideoReferenceUuid();
        imagedMomentUuid = imageReference.getUuid();
        timecode = annotation.getTimecode();
        elapsedTime = annotation.getElapsedTime();
        recordedTimestamp = annotation.getRecordedTimestamp();
    }

    public UUID getImageReferenceUuid() {
        return imageReferenceUuid;
    }

    public String getFormat() {
        return format;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public URL getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public UUID getVideoReferenceUuid() {
        return videoReferenceUuid;
    }

    public UUID getImagedMomentUuid() {
        return imagedMomentUuid;
    }

    public Timecode getTimecode() {
        return timecode;
    }

    public Duration getElapsedTime() {
        return elapsedTime;
    }

    public Instant getRecordedTimestamp() {
        return recordedTimestamp;
    }

    public void setImageReferenceUuid(UUID imageReferenceUuid) {
        this.imageReferenceUuid = imageReferenceUuid;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setVideoReferenceUuid(UUID videoReferenceUuid) {
        this.videoReferenceUuid = videoReferenceUuid;
    }

    public void setImagedMomentUuid(UUID imagedMomentUuid) {
        this.imagedMomentUuid = imagedMomentUuid;
    }

    public void setTimecode(Timecode timecode) {
        this.timecode = timecode;
    }

    public void setElapsedTime(Duration elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public void setRecordedTimestamp(Instant recordedTimestamp) {
        this.recordedTimestamp = recordedTimestamp;
    }

    public VideoIndex getVideoIndex() {
        return new VideoIndex(Optional.ofNullable(recordedTimestamp),
                Optional.ofNullable(elapsedTime),
                Optional.ofNullable(timecode));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (description != null) {
            sb.append(description);
        }
        else {
            sb.append("Undescribed Image");
        }
        if (format != null) {
            sb.append(" [")
                    .append(format)
                    .append("]");
        }
        if (width != null && height != null) {
            sb.append(" ")
                    .append(width)
                    .append("x")
                    .append(height);
        }
        return sb.toString();
    }
}
