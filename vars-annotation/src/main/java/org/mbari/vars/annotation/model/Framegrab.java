package org.mbari.vars.annotation.model;

import org.mbari.vcr4j.VideoIndex;

import java.util.Optional;

/**
 * @author Brian Schlining
 * @since 2017-08-31T15:03:00
 */
public class Framegrab {

    private java.awt.Image image;
    private VideoIndex videoIndex;

    public Framegrab() {}

    public Framegrab(java.awt.Image image) {
        this.image = image;
    }

    public Framegrab(java.awt.Image image, VideoIndex videoIndex) {
        this.image = image;
        this.videoIndex = videoIndex;
    }

    public Optional<java.awt.Image> getImage() {
        return Optional.ofNullable(image);
    }

    public Optional<VideoIndex> getVideoIndex() {
        return Optional.ofNullable(videoIndex);
    }

    public void setImage(java.awt.Image image) {
        this.image = image;
    }

    public void setVideoIndex(VideoIndex videoIndex) {
        this.videoIndex = videoIndex;
    }

    public boolean isComplete() {
        return image != null && videoIndex != null;
    }
}
