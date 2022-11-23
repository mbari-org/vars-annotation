package org.mbari.vars.services.model;

import com.google.gson.annotations.SerializedName;

import java.net.URL;
import java.time.Instant;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-05-11T14:01:00
 */
public class ImageReference implements Cloneable {
    private UUID uuid;
    private String description;
    private URL url;
    private String format;
    private Instant lastUpdatedTime;

    @SerializedName("width_pixels")
    private Integer width;

    @SerializedName("height_pixels")
    private Integer height;

    public ImageReference() {
    }

    /**
     * Copy constructor
     * @param i
     */
    public ImageReference(ImageReference i) {
        uuid = i.uuid;
        description = i.description;
        url = i.url;
        format = i.format;
        lastUpdatedTime = i.lastUpdatedTime;
        width = i.width;
        height = i.height;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Instant getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(Instant lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }
}
