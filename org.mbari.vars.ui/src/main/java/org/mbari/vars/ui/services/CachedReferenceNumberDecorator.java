package org.mbari.vars.ui.services;

import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.MediaChangedEvent;

import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.MediaService;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.services.model.ConceptAssociation;
import org.mbari.vars.services.model.ConceptAssociationRequest;
import org.mbari.vars.services.model.Media;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2019-06-04T16:03:00
 */
public class CachedReferenceNumberDecorator {


    private final UIToolBox toolBox;
    private final AnnotationService annotationService;
    private final MediaService mediaService;
    private final List<Media> medias = new CopyOnWriteArrayList<>();
    private final String associationKey;
    private final List<ConceptAssociation> conceptAssociations = new CopyOnWriteArrayList<>();
    private final Logger log = LoggerFactory.getLogger(getClass());

    public CachedReferenceNumberDecorator(UIToolBox toolBox) {
        this.toolBox = toolBox;
        this.annotationService = toolBox.getServices().getAnnotationService();
        this.mediaService = toolBox.getServices().getMediaService();
        associationKey = toolBox.getConfig()
                .getString("app.annotation.identity.reference");
        toolBox.getEventBus()
                .toObserverable()
                .ofType(MediaChangedEvent.class)
                .subscribe(evt -> clear());
    }

    public synchronized void clear() {
        log.info("Clearing cached '" + associationKey + "' references");
        medias.clear();
        conceptAssociations.clear();
    }

    private CompletableFuture<List<Media>> findRelatedMedia(Media media) {
        if (!medias.contains(media)) {
            clear();
        }

        if (medias.isEmpty()) {
           return  mediaService.findByVideoSequenceName(media.getVideoSequenceName())
                    .thenApply(ms -> {
                       medias.addAll(ms);
                       return ms;
                    });
        }
        else {
            return CompletableFuture.completedFuture(medias);
        }
    }

    private CompletableFuture<List<ConceptAssociation>> findExistingReferenceNumbers(Media media) {
        if (conceptAssociations.isEmpty()) {
            return findRelatedMedia(media)
                    .thenCompose(ms -> {
                        List<UUID> videoReferenceUuids = ms.stream()
                                .map(Media::getVideoReferenceUuid)
                                .collect(Collectors.toList());
                        ConceptAssociationRequest request = new ConceptAssociationRequest(associationKey, videoReferenceUuids);
                        return annotationService.findByConceptAssociationRequest(request)
                                .thenApply(response -> {
                                    conceptAssociations.addAll(response.getConceptAssociations());
                                    return conceptAssociations;
                                });
                    });
        }
        else {
            return CompletableFuture.completedFuture(conceptAssociations);
        }
    }

    public CompletableFuture<List<Association>> findOldReferenceNumbers(Media media, String concept) {

        return findExistingReferenceNumbers(media)
                .thenApply(cas -> {
                    List<Association> remoteAssociations = cas.stream()
                            .filter(ca -> ca.getConcept().equals(concept))
                            .map(ConceptAssociation::asAssociation)
                            .collect(Collectors.toCollection(ArrayList::new));

                    remoteAssociations.addAll(currentReferences(concept));
                    List<Association> as = remoteAssociations.stream()
                            .sorted(Association.IDENTITY_REF_NUM_COMPARATOR)
                            .collect(Collectors.toList());
                    return as;
                });

    }


    public CompletableFuture<List<Association>> findNewReferenceNumbers(Media media) {

        return findExistingReferenceNumbers(media)
                .thenApply(cas -> {
                    List<Association> remoteAssociations = cas.stream()
                            .map(ConceptAssociation::asAssociation)
                            .collect(Collectors.toCollection(ArrayList::new));
                    remoteAssociations.addAll(currentReferences());
                    return remoteAssociations.stream()
                            .sorted(Association.IDENTITY_REF_NUM_COMPARATOR)
                            .collect(Collectors.toList());
                });
    }

    private List<Association> currentReferences() {
        return toolBox.getData()
                .getAnnotations()
                .stream()
                .flatMap(a -> a.getAssociations().stream())
                .filter(a -> a.getLinkName().equals(associationKey))
                .collect(Collectors.toList());
    }

    private List<Association> currentReferences(String concept) {
        return toolBox.getData()
                .getAnnotations()
                .stream()
                .filter(a -> a.getConcept().equals(concept))
                .flatMap(a -> a.getAssociations().stream())
                .filter(a -> a.getLinkName().equals(associationKey))
                .collect(Collectors.toList());
    }




}
