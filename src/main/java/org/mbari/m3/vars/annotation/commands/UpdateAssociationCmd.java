package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.events.AnnotationsChangedEvent;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.services.AnnotationService;

import java.util.Arrays;
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
        doUpdate(toolBox, newAssociation);
    }

    @Override
    public void unapply(UIToolBox toolBox) {
        doUpdate(toolBox, oldAssociation);
    }

    private void doUpdate(UIToolBox toolBox, Association association) {
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        annotationService
                .updateAssociation(association)
                .thenAccept(a -> annotationService.findByUuid(observationUuid)
                        .thenAccept(annotation -> toolBox.getEventBus()
                                .send(new AnnotationsChangedEvent(Arrays.asList(annotation)))));
    }

    @Override
    public String getDescription() {
        return "Update Association: " + newAssociation;
    }
}