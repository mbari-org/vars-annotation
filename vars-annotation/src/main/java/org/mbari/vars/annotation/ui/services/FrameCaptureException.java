package org.mbari.vars.annotation.ui.services;

/**
 * Thrown when a frame can not be captured from the media player. The message describes
 * the actual reason for the failure so that it can be shown to the user.
 *
 * @author Brian Schlining
 * @since 2026-10-08
 */
public class FrameCaptureException extends RuntimeException {

    public FrameCaptureException(String message) {
        super(message);
    }

    public FrameCaptureException(String message, Throwable cause) {
        super(message, cause);
    }
}
