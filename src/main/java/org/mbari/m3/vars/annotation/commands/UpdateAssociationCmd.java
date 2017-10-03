package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.m3.vars.annotation.services.ConceptService;
import org.mbari.m3.vars.annotation.ui.AnnotationServiceDecorator;

import java.util.UUID;

/**
 * @author Brian Schlining
 * @since 2017-05-10T10:06:00
 */
public class UpdateAssociationCmd implements Command {

    private final UUID observationUuid;
    private final Association oldAssociation;
    private final Association newAssociation;

    public UpdateAssociationCmd(UUID observationUuid, Association oldAssociation, Association newAssociation) {
        this.observationUuid = observationUuid;
        this.oldAssociation = oldAssociation;
        this.newAssociation = newAssociation;
    }

    @Override
    public void apply(UIToolBox toolBox) {
        ConceptService conceptService = toolBox.getServices().getConceptService();
        // Make sure we're using a primary name in the toConcept
        conceptService.findConcept(newAssociation.getToConcept())
                .thenAccept(opt -> {
                    Association a = opt.map(c -> new Association(newAssociation.getLinkName(),
                            opt.get().getName(),
                            newAssociation.getLinkValue(),
                            newAssociation.getMimeType(),
                            newAssociation.getUuid())).orElse(newAssociation);
                    doUpdate(toolBox, a);
                });
    }

    @Override
    public void unapply(UIToolBox toolBox) {
        doUpdate(toolBox, oldAssociation);
    }

    private void doUpdate(UIToolBox toolBox, Association association) {
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        annotationService.updateAssociation(association)
                .thenAccept(a -> {
                    AnnotationServiceDecorator decorator = new AnnotationServiceDecorator(toolBox);
                    decorator.refreshAnnotationsView(observationUuid);
                });
    }

    @Override
    public String getDescription() {
        return "Update Association: " + newAssociation;
    }
}