package org.mbari.m3.vars.annotation.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Changes the media that is currently being edited
 * @author Brian Schlining
 * @since 2017-06-28T13:09:00
 */
public class ChangeMediaMsg implements Message {


    private final UUID videoReferenceUuid;

    public ChangeMediaMsg(UUID videoReferenceUuid) {
        this.videoReferenceUuid = videoReferenceUuid;
    }

    public UUID getVideoReferenceUuid() {
        return videoReferenceUuid;
    }
}
