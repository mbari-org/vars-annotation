package org.mbari.vars.annotation.ui.commands;

import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;
import org.mbari.vars.annosaurus.sdk.r1.AnnotationService;
import org.mbari.vars.annotation.ui.javafx.AnnotationServiceDecorator;
import org.mbari.vars.annotation.util.Preconditions;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-05-11T13:06:00
 */
public class DeleteAssociationsCmd implements Command {

    /** key = an association attached to that obervaton, value = observationUuid, */
    private Map<Association, UUID> associationMap;
    /** Source of the change event for the first apply only. Undo and redo happen in VARS, so they use none. */
    private final Object eventSource;
    private volatile boolean applied = false;

    public DeleteAssociationsCmd(Map<Association, UUID> associations) {
        this(associations, null);
    }

    /**
     * @param eventSource The source of the AnnotationsChangedEvent sent after the first apply. Lets
     *                    listeners tell where the change came from (e.g. Sharktopoda).
     */
    public DeleteAssociationsCmd(Map<Association, UUID> associations, Object eventSource) {
        Preconditions.checkArgument(associations != null,
                "Can not delete a null assotation map");
        Preconditions.checkArgument(!associations.isEmpty(),
                "Can not delete an empty association map");
        this.associationMap = Collections.unmodifiableMap(new HashMap<>(associations));
        this.eventSource = eventSource;
    }

    public Object getEventSource() {
        return eventSource;
    }

    @Override
    public void apply(UIToolBox toolBox) {
        Object source = applied ? null : eventSource;
        applied = true;
        AnnotationService service = toolBox.getServices().annotationService();
        Collection<UUID> uuids = associationMap.keySet()
                .stream()
                .map(Association::getUuid)
                .collect(Collectors.toList());
        service.deleteAssociations(uuids)
                .thenAccept(v -> {
                    Set<UUID> observationUuids = new HashSet<>(associationMap.values());
                    AnnotationServiceDecorator decorator = new AnnotationServiceDecorator(toolBox);
                    decorator.refreshAnnotationsView(observationUuids, source);
                });
    }

    @Override
    public void unapply(UIToolBox toolBox) {
        AnnotationService service = toolBox.getServices().annotationService();
        Map<Association, UUID> newMap = new ConcurrentHashMap<>();
        CompletableFuture[] futures = associationMap.entrySet()
                .stream()
                .map(e -> service.createAssociation(e.getValue(), e.getKey())
                        .thenAccept(a -> newMap.put(a, e.getValue())))  // Need to collect new Associations as the UUID will have changed
                .toArray(i -> new CompletableFuture[i]);
        CompletableFuture.allOf(futures)
                .thenAccept(v -> {
                    associationMap = newMap;  // Update stored associations with the new keys
                    Set<UUID> observationUuids = new HashSet<>(associationMap.values());
                    AnnotationServiceDecorator decorator = new AnnotationServiceDecorator(toolBox);
                    decorator.refreshAnnotationsView(observationUuids);
                });
    }

    @Override
    public String getDescription() {
        return "Delete Associations";
    }

}
