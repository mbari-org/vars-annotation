package org.mbari.vars.ui.commands;

import org.mbari.vars.ui.Data;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Media;
import org.mbari.vcr4j.VideoIndex;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-08-31T17:02:00
 */
public class CommandUtil {

    public static Annotation buildAnnotation(Data data, String concept, VideoIndex videoIndex) {
        Media media = data.getMedia();
        UUID videoReferenceUuid = media.getVideoReferenceUuid();
        String observer = data.getUser().getUsername();
        String group = data.getGroup();
        String activity = data.getActivity();
        Annotation a0 = new Annotation(concept,
                observer,
                videoIndex,
                videoReferenceUuid);
        a0.setGroup(group);
        a0.setActivity(activity);
        videoIndex.getTimestamp().ifPresent(a0::setRecordedTimestamp);
        videoIndex.getElapsedTime().ifPresent(a0::setElapsedTime);
        videoIndex.getTimecode().ifPresent(a0::setTimecode);
        if (media.getStartTimestamp() != null ) {
            // Calculate timestamp from media start time and annotation elapsed time
            videoIndex.getElapsedTime()
                    .ifPresent(elapsedTime -> {
                        Instant recordedDate = media.getStartTimestamp().plus(elapsedTime);
                        a0.setRecordedTimestamp(recordedDate);
                    });
        }
        return a0;
    }

    public static String getDeploymentId(Media media) {
        String cameraId = media.getCameraId();
        String deploymentId = media.getVideoSequenceName();
        int i = deploymentId.indexOf(cameraId);
        if (i >= 0) {
            deploymentId = deploymentId.substring(i, deploymentId.length() - i);
        }
        return deploymentId;
    }
}
