package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.model.Annotation;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-09-17T21:38:00
 */
public class ChangeVideoReferenceCmd extends UpdateAnnotationsCmd {

    private final UUID videoReferenceUuid;


    public ChangeVideoReferenceCmd(List<Annotation> originalAnnotations, UUID videoReferenceUuid) {
        super(originalAnnotations, originalAnnotations.stream()
                .map(Annotation::new)
                .peek(a -> a.setVideoReferenceUuid(videoReferenceUuid))
                .collect(Collectors.toList()));
        this.videoReferenceUuid = videoReferenceUuid;
    }


    @Override
    public String getDescription() {
        return "Changing videoReferenceUuid of " + originalAnnotations.size() +
                " annotations to " + videoReferenceUuid;
    }
}