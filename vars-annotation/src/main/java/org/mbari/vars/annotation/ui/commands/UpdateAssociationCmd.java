package org.mbari.vars.annotation.ui.commands;

import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;
import org.mbari.vars.annosaurus.sdk.r1.AnnotationService;
import org.mbari.vars.oni.sdk.r1.ConceptService;
import org.mbari.vars.annotation.ui.javafx.AnnotationServiceDecorator;

import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-05-10T10:06:00
 */
public class UpdateAssociationCmd implements Command {

    private final UUID observationUuid;
    private final Association oldAssociation;
    private final Association newAssociation;
    /** Source of the change event for the first apply only. Undo and redo happen in VARS, so they use none. */
    private final Object eventSource;
    private volatile boolean applied = false;

    public UpdateAssociationCmd(UUID observationUuid, Association oldAssociation, Association newAssociation) {
        this(observationUuid, oldAssociation, newAssociation, null);
    }

    /**
     * @param eventSource The source of the AnnotationsChangedEvent sent after the first apply. Lets
     *                    listeners tell where the change came from (e.g. Sharktopoda).
     */
    public UpdateAssociationCmd(UUID observationUuid,
                                Association oldAssociation,
                                Association newAssociation,
                                Object eventSource) {
        this.observationUuid = observationUuid;
        this.oldAssociation = oldAssociation;
        this.newAssociation = newAssociation;
        this.eventSource = eventSource;
    }

    public Object getEventSource() {
        return eventSource;
    }

    @Override
    public void apply(UIToolBox toolBox) {
        Object source = applied ? null : eventSource;
        applied = true;
        ConceptService conceptService = toolBox.getServices().conceptService();
        // Make sure we're using a primary name in the toConcept
        conceptService.findConcept(newAssociation.getToConcept())
                .thenAccept(opt -> {
                    Association a = opt.map(c -> new Association(newAssociation.getLinkName(),
                            opt.get().getName(),
                            newAssociation.getLinkValue(),
                            newAssociation.getMimeType(),
                            newAssociation.getUuid())).orElse(newAssociation);
                    doUpdate(toolBox, a, source);
                });
    }

    @Override
    public void unapply(UIToolBox toolBox) {
        doUpdate(toolBox, oldAssociation, null);
    }

    private void doUpdate(UIToolBox toolBox, Association association, Object source) {
        AnnotationService annotationService = toolBox.getServices().annotationService();
        annotationService.updateAssociation(association)
                .thenAccept(a -> {
                    AnnotationServiceDecorator decorator = new AnnotationServiceDecorator(toolBox);
                    decorator.refreshAnnotationsView(observationUuid, source);
                });
    }

    @Override
    public String getDescription() {
        return "Update Association: " + newAssociation;
    }
}