package org.mbari.vars.annotation.ui.messages;

import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;

/**
 * Published after the media player has been told to seek to an annotation (i.e. a {@link SeekMsg}
 * that carries an annotation). Sent <b>after</b> the seek so that anything it triggers, such as
 * re-selecting the annotation's localizations in the video player, reaches the player after the seek.
 *
 * @param annotation The annotation that was seeked to
 */
public record SeekedToAnnotationMsg(Annotation annotation) {
}
