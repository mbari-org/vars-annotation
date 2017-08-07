package org.mbari.m3.vars.annotation.messages;

import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-07-20T22:09:00
 */
public interface Message {
    /**
     * Changes the media that is currently being edited
     * @author Brian Schlining
     * @since 2017-06-28T13:09:00
     */
    class ChangeMediaMsg implements Message {


        private final UUID videoReferenceUuid;

        public ChangeMediaMsg(UUID videoReferenceUuid) {
            this.videoReferenceUuid = videoReferenceUuid;
        }

        public UUID getVideoReferenceUuid() {
            return videoReferenceUuid;
        }
    }
}
