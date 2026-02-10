package org.mbari.vars.annotation.ui.commands;

import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.oni.sdk.r1.models.User;
import org.mbari.vars.oni.sdk.r1.ConceptService;
import org.mbari.vars.annotation.ui.javafx.AnnotationServiceDecorator;
import org.mbari.vcr4j.util.Preconditions;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-09-17T16:24:00
 */
public abstract class UpdateAnnotationsCmd implements Command {

    protected List<Annotation> originalAnnotations;
    protected List<Annotation> changedAnnotations;
    private volatile boolean checkConceptName;
    private boolean updateUser = false;

    public UpdateAnnotationsCmd(List<Annotation> originalAnnotations,
                                List<Annotation> changedAnnotations) {
        this(originalAnnotations, changedAnnotations, false);
    }

    /**
     *
     * @param originalAnnotations The uncahgned annotations
     * @param changedAnnotations
     * @param checkConceptName
     */
    public UpdateAnnotationsCmd(List<Annotation> originalAnnotations,
                                List<Annotation> changedAnnotations,
                                boolean checkConceptName) {
        this(originalAnnotations, changedAnnotations, checkConceptName, false);
    }

    public UpdateAnnotationsCmd(List<Annotation> originalAnnotations,
                                List<Annotation> changedAnnotations,
                                boolean checkConceptName,
                                boolean updateUser) {
        Preconditions.checkArgument(originalAnnotations != null,
                "Original annotations can not be null");
        Preconditions.checkArgument(changedAnnotations != null,
                "Changed annotations can not be null");
        Preconditions.checkArgument(originalAnnotations.size() == changedAnnotations.size(),
                "The Original annotations and the changed annotations are not the same size");
        this.originalAnnotations = originalAnnotations;
        this.changedAnnotations = changedAnnotations;
        final Instant now = Instant.now();
        changedAnnotations.forEach(a -> a.setObservationTimestamp(now));
        this.checkConceptName = checkConceptName;
        this.updateUser = updateUser;
    }

    @Override
    public void apply(UIToolBox toolBox) {
        // Dont' change user when activity is changed.
        if (updateUser) {
            final User user = toolBox.getData().getUser();
            if (user != null) {
                changedAnnotations.forEach(a -> a.setObserver(user.getUsername()));
            }
        }
        if (checkConceptName) {
            ConceptService conceptService = toolBox.getServices().conceptService();

            CompletableFuture[] futures = changedAnnotations.stream()
                    .map(a -> conceptService.findConcept(a.getConcept())
                                .thenAccept(opt ->
                                        opt.ifPresent(c -> a.setConcept(c.getName()))))
                    .toArray(i -> new CompletableFuture[i]);
            CompletableFuture.allOf(futures)
                    .thenAccept(v -> {
                        doUpdate(toolBox, changedAnnotations);
                        checkConceptName = false;
                    });
        }
        else {
            doUpdate(toolBox, changedAnnotations);
        }


    }

    @Override
    public void unapply(UIToolBox toolBox) {
        doUpdate(toolBox, originalAnnotations);
    }

    private void doUpdate(UIToolBox toolBox, List<Annotation> annotations) {
        toolBox.getServices()
                .annotationService()
                .updateAnnotations(annotations)
                .thenAccept(as -> {
                    AnnotationServiceDecorator asd = new AnnotationServiceDecorator(toolBox);
                    Set<UUID> uuids = originalAnnotations.stream()
                            .map(Annotation::getObservationUuid)
                            .collect(Collectors.toSet());
                    asd.refreshAnnotationsView(uuids);
                });
    }


}
