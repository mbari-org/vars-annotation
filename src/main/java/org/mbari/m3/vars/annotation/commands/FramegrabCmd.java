package org.mbari.m3.vars.annotation.commands;

import org.mbari.awt.image.ImageUtilities;
import org.mbari.io.FileUtilities;
import org.mbari.m3.vars.annotation.Data;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsAddedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayer;
import org.mbari.m3.vars.annotation.messages.ShowWarningAlert;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Framegrab;
import org.mbari.m3.vars.annotation.model.Image;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.m3.vars.annotation.services.ImageCaptureService;
import org.mbari.m3.vars.annotation.ui.shared.AlertWarningController;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.VideoState;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.time.Instant;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-08-31T14:43:00
 */
public class FramegrabCmd implements Command {

    private volatile Annotation annotation;

    @Override
    public void apply(UIToolBox toolBox) {
        ResourceBundle i18n = toolBox.getI18nBundle();

        Media media = toolBox.getData().getMedia();
        if (media == null) {
            toolBox.getEventBus()
                    .send(new ShowWarningAlert(i18n.getString("commands.framecapture.title"),
                            i18n.getString("commands.framecapture.header"),
                            i18n.getString("commands.framecapture.nomedia.content")));
            return;
        }

        MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer = toolBox.getMediaPlayer();
        if (mediaPlayer == null) {
            toolBox.getEventBus()
                    .send(new ShowWarningAlert(i18n.getString("commands.framecapture.title"),
                            i18n.getString("commands.framecapture.header"),
                            i18n.getString("commands.framecapture.nomediaplayer.content")));
            return;
        }

        // Capture image
        Framegrab framegrab;
        String deploymentId = CommandUtil.getDeploymentId(media);
        File imagePath;
        try {
            File image = File.createTempFile(deploymentId, ".png",
                    Initializer.getImageDirectory().toFile());
            image.deleteOnExit();

            ImageCaptureService service = mediaPlayer
                    .getImageCaptureService();

            Framegrab framegrab0 = service.capture(image);
            framegrab0.getVideoIndex().ifPresent(videoIndex -> {
                        videoIndex.getElapsedTime().ifPresent(elapsedTime -> {
                            Instant recordedDate = media.getStartTimestamp().plus(elapsedTime);
                            framegrab0.setVideoIndex(new VideoIndex(elapsedTime, recordedDate));
                        });
                    });
            framegrab = framegrab0;
            imagePath = image;
        }
        catch (Exception e) {
            // TODO show error
            return;
        }

        // Create image

        framegrab.getImage().ifPresent(awtImg -> {
            BufferedImage bi = ImageUtilities.toBufferedImage(awtImg);
            Image image = new Image();
            image.setFormat("image/png");
            image.setHeight(bi.getHeight());
            image.setWidth(bi.getWidth());
            image.setVideoReferenceUuid(media.getVideoReferenceUuid());
            image.setDescription("uncompressed image");
            try {
                image.setUrl(imagePath.toURI().toURL());
            }
            catch (MalformedURLException e) {
                // TODO stop processing. show error
            }


            framegrab.getVideoIndex().ifPresent(videoIndex -> {
                videoIndex.getElapsedTime().ifPresent(image::setElapsedTime);
                videoIndex.getTimecode().ifPresent(image::setTimecode);
                videoIndex.getTimestamp().ifPresent(image::setRecordedTimestamp);

                AnnotationService annotationService = toolBox.getServices().getAnnotationService();
                annotationService.createImage(image)
                        .thenAccept(image1 -> {
                            // Create annotation
                            toolBox.getServices()
                                    .getConceptService()
                                    .findRoot()
                                    .thenAccept(root -> {
                                        Annotation a0 = CommandUtil.buildAnnotation(toolBox.getData(),
                                                root.getName(), videoIndex);
                                        annotationService.createAnnotation(a0)
                                                .thenAccept(a1 -> {
                                                    annotation = a1;
                                                    EventBus eventBus = toolBox.getEventBus();
                                                    eventBus.send(new AnnotationsAddedEvent(a1));
                                                    eventBus.send(new AnnotationsSelectedEvent(a1));
                                                });

                                    });



                            // TODO Upload image and update image URL
                        });

            });



        });











    }

    @Override
    public void unapply(UIToolBox toolBox) {

    }

    @Override
    public String getDescription() {
        return null;
    }




}
