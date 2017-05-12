package org.mbari.m3.vars.annotation.model;

import java.net.URL;
import java.time.Instant;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-05-11T14:01:00
 */
public class ImageReference {
    private UUID uuid;
    private String description;
    private URL url;
    private String format;
    private Instant lastUpdatedTime;
}
