package org.mbari.vars.ui.commands;

import com.google.common.base.Preconditions;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.ui.javafx.AnnotationServiceDecorator;

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

    public DeleteAssociationsCmd(Map<Association, UUID> associations) {
        Preconditions.checkArgument(associations != null,
                "Can not delete a null assotation map");
        Preconditions.checkArgument(!associations.isEmpty(),
                "Can not delete an empty association map");
        this.associationMap = Collections.unmodifiableMap(new HashMap<>(associations));
    }

    @Override
    public void apply(UIToolBox toolBox) {
        AnnotationService service = toolBox.getServices().getAnnotationService();
        Collection<UUID> uuids = associationMap.keySet()
                .stream()
                .map(Association::getUuid)
                .collect(Collectors.toList());
        service.deleteAssociations(uuids)
                .thenAccept(v -> {
                    Set<UUID> observationUuids = new HashSet<>(associationMap.values());
                    AnnotationServiceDecorator decorator = new AnnotationServiceDecorator(toolBox);
                    decorator.refreshAnnotationsView(observationUuids);
                });
    }

    @Override
    public void unapply(UIToolBox toolBox) {
        AnnotationService service = toolBox.getServices().getAnnotationService();
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
