package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsAddedEvent;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayer;
import org.mbari.m3.vars.annotation.messages.ShowWarningAlert;
import org.mbari.m3.vars.annotation.model.*;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.m3.vars.annotation.ui.AnnotationServiceDecorator;
import org.mbari.m3.vars.annotation.ui.ImageArchiveServiceDecorator;
import org.mbari.m3.vars.annotation.services.ImageCaptureService;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.VideoState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-08-31T14:43:00
 */
public class FramegrabCmd implements Command {

    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'")
            .withZone(ZoneOffset.UTC);

    private volatile Annotation annotationRef;
    private volatile Image imageRef;
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void apply(UIToolBox toolBox) {

        if (annotationRef != null && imageRef != null) {
            skipTheBS(toolBox);
            return;
        }

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

        // -- Capture image
        File imageFile = ImageArchiveServiceDecorator.buildLocalImageFile(media, ".png");
        Optional<Framegrab> framegrabOpt = capture(imageFile, media, mediaPlayer);
        if (!framegrabOpt.isPresent()) {
            toolBox.getEventBus()
                    .send(new ShowWarningAlert(
                            i18n.getString("commands.framecapture.title"),
                            i18n.getString("commands.framecapture.header"),
                            i18n.getString("commands.framecapture.nomedia.content") +
                                    " " + imageFile.getAbsolutePath()
                    ));
            return;
        }
        log.debug("Captured framegrab locally to {}", imageFile.getAbsolutePath());

        URL url;
        try {
            url = imageFile.toURI().toURL();
        } catch (MalformedURLException e) {
            toolBox.getEventBus()
                    .send(new ShowWarningAlert(
                            i18n.getString("commands.framecapture.title"),
                            i18n.getString("commands.framecapture.header"),
                            i18n.getString("commands.framecapture.badurl.content") +
                                    " " + imageFile.getAbsolutePath()
                    ));
            return;
        }

        AnnotationService annotationService = toolBox.getServices()
                .getAnnotationService();
        AnnotationServiceDecorator asd = new AnnotationServiceDecorator(toolBox);
        EventBus eventBus = toolBox.getEventBus();
        Framegrab framegrab = framegrabOpt.get();
        ImageArchiveServiceDecorator.createImageInDatastore(toolBox, media, framegrab, url).thenAccept(image ->
                createAnnotationInDatastore(toolBox, image)
                        .thenApply(a1 -> {
                            // notify UI
                            log.debug("Created annotation for framegrab: {}" + a1);
                            eventBus.send(new AnnotationsAddedEvent(a1));
                            eventBus.send(new AnnotationsSelectedEvent(a1));
                            return a1;
                        })
                        .thenApply(a1 -> {
                            // upload image
                            archiveImage(toolBox, media, image, imageFile.toPath()).handle((iur, ex) -> {
                                if (ex != null) {
                                    log.debug("Failed to archive framegrab with image service", ex);
                                    annotationService.deleteImage(image.getImageReferenceUuid());
                                    asd.refreshAnnotationsView(a1.getObservationUuid());
                                    imageFile.delete();
                                } else {
                                    log.debug("Uploaded framegrab to {}", iur.getUri());
                                    // update URL used by images in annotations
                                    updateAnnotationWithArchivedImage(toolBox, image, iur)
                                            .handle((annos, ex1) -> {
                                                if (ex1 != null) {
                                                    log.debug("Failed to update annotation with remote framegrab URI", ex1);
                                                    annotationService.deleteImage(image.getImageReferenceUuid());
                                                    asd.refreshAnnotationsView(a1.getObservationUuid());
                                                    imageFile.delete();
                                                }
                                                else {
                                                    // Notify UI
                                                    log.debug("Updated annotation with remote framegrab URI");
                                                    Set<UUID> uuids = toolBox.getData()
                                                            .getSelectedAnnotations()
                                                            .stream()
                                                            .map(Annotation::getObservationUuid)
                                                            .collect(Collectors.toSet());
                                                    asd.refreshAnnotationsView(uuids);
                                                }
                                                return null;
                                            })
                                            .thenAccept(v -> imageFile.delete())
                                            .thenAccept(v -> {
                                                log.debug("Compressing framegrab");
                                                ImageArchiveServiceDecorator decorator = new ImageArchiveServiceDecorator(toolBox);
                                                decorator.compressFramegrab(media, framegrab, iur);
                                            });
                                }
                                return null;
                            });
                            return null;
                        }));


    }

    @Override
    public void unapply(UIToolBox toolBox) {
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        if (annotationRef != null && imageRef != null) {
            // Delete annotations
            // Find ones that share the same image ref
            // Delete image ref
            // Republish other annotations so UI updates
            annotationService.deleteAnnotation(annotationRef.getObservationUuid())
                    .thenAccept(v -> ImageArchiveServiceDecorator.refreshRelatedAnnotations(toolBox, imageRef.getImageReferenceUuid(), true));
        }
    }

    @Override
    public String getDescription() {
        return "Framegrab";
    }



    private void skipTheBS(UIToolBox toolBox) {
        AnnotationService annotationService  = toolBox.getServices().getAnnotationService();
        annotationService.createImage(imageRef)
                .thenAccept(img -> annotationService.createAnnotation(annotationRef)
                        .thenAccept(a -> ImageArchiveServiceDecorator.refreshRelatedAnnotations(toolBox, img.getImageReferenceUuid(), false)));
    }

    private CompletableFuture<Annotation> createAnnotationInDatastore(UIToolBox toolBox, Image image) {
        CompletableFuture<Annotation> f0 = new CompletableFuture<>();
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        VideoIndex videoIndex = image.getVideoIndex();
        toolBox.getServices()
                .getConceptService()
                .findRoot()
                .thenAccept(root -> {
                    Annotation a0 = CommandUtil.buildAnnotation(toolBox.getData(),
                            root.getName(), videoIndex);
                    annotationService.createAnnotation(a0)
                            .thenAccept(a -> {
                                annotationRef = a;
                                f0.complete(a);
                            });

                });
        return f0;
    }


    public Optional<Framegrab> capture(File imageFile, Media media, MediaPlayer<? extends VideoState, ? extends VideoError> mediaPlayer) {
        try {
            ImageCaptureService service = mediaPlayer
                    .getImageCaptureService();

            Framegrab framegrab0 = service.capture(imageFile);
            framegrab0.getVideoIndex().ifPresent(videoIndex -> {
                videoIndex.getElapsedTime().ifPresent(elapsedTime -> {
                    Instant recordedDate = media.getStartTimestamp().plus(elapsedTime);
                    framegrab0.setVideoIndex(new VideoIndex(elapsedTime, recordedDate));
                });
            });
            return Optional.of(framegrab0);
        }
        catch (Exception e) {
            // TODO show error
            return Optional.empty();
        }
    }

    private CompletableFuture<List<Annotation>> updateAnnotationWithArchivedImage(UIToolBox toolBox,
                                                                                  Image image,
                                                                                  ImageUploadResults imageUploadResults) {
        CompletableFuture<List<Annotation>> cf = new CompletableFuture<>();
        URL url;
        try {
            url = imageUploadResults.getUri().toURL();
        } catch (MalformedURLException e) {
            cf.completeExceptionally(e);
            return cf;
        }

        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        annotationService.findImageByUrl(url)
                .thenAccept(img0 -> {
                    if (img0 == null) {
                        image.setUrl(url);
                        annotationService.updateImage(image)
                                .thenAccept(img1 -> {
                                    imageRef = img1;
                                    annotationService.findByImageReference(image.getImageReferenceUuid())
                                            .thenAccept(cf::complete);
                                });
                    }
                    else {
                        cf.completeExceptionally(new RuntimeException("The image at " +
                                url.toExternalForm() + " already exists"));
                    }
                });

        return cf;
    }


    private CompletableFuture<ImageUploadResults> archiveImage(UIToolBox toolBox, Media media, Image image, Path imagePath) {
        String name = ImageArchiveServiceDecorator.buildName(image.getVideoReferenceUuid(), image.getVideoIndex(), ".png");
        String deploymentId = CommandUtil.getDeploymentId(media);
        return toolBox.getServices()
                .getImageArchiveService()
                .upload(media.getCameraId(),
                        deploymentId,
                        name,
                        imagePath);
    }




}
