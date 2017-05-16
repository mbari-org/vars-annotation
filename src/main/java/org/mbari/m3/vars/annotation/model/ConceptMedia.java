package org.mbari.m3.vars.annotation.model;

import java.net.URL;

/**
 * @author Brian Schlining
 * @since 2017-05-11T15:54:00
 */
public class ConceptMedia {
    private URL url;
    private String caption;
    private String credit;
    private String mimeType;
    private boolean isPrimary = false;

    public ConceptMedia(URL url, String caption, String credit, String mimeType, boolean isPrimary) {
        this.url = url;
        this.caption = caption;
        this.credit = credit;
        this.mimeType = mimeType;
        this.isPrimary = isPrimary;
    }

    public URL getUrl() {
        return url;
    }

    public String getCaption() {
        return caption;
    }

    public String getCredit() {
        return credit;
    }

    public String getMimeType() {
        return mimeType;
    }

    public boolean isPrimary() {
        return isPrimary;
    }
}
