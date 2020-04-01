package org.mbari.vars.ui.commands;

import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.javafx.AnnotationServiceDecorator;
import org.mbari.vcr4j.util.Preconditions;

/**
 * This is a specialized create association command for those cases, such
 * as when a boundin box is created in Cthulhu where assocations have
 * a UUID before being persisted and that UUID needs to be preserved.
 *
 * If that is not your use case, use `CreateAssociationsCmd` instead.
 */
public class CreateAssociationCmd implements Command {

    private final Association association;
    private final Annotation annotation;
    private Association createdAssociation;

    public CreateAssociationCmd(Association association, Annotation annotation) {
        Preconditions.checkArgument(association.getUuid() != null,
                "This command requires that the association have a UUID");

        this.association = association;
        this.annotation = annotation;
    }

    public Association getAssociation() {
        return association;
    }

    @Override
    public void apply(UIToolBox toolBox) {
        AnnotationService annotationService = toolBox.getServices().getAnnotationService();
        annotationService.createAssociation(annotation.getObservationUuid(), association, association.getUuid())
                .thenAccept(ass -> {
                    createdAssociation = ass;
                    AnnotationServiceDecorator asd = new AnnotationServiceDecorator(toolBox);
                    asd.refreshAnnotationsView(annotation.getObservationUuid());
                });
    }

    @Override
    public void unapply(UIToolBox toolBox) {
        if (createdAssociation != null) {
            AnnotationService annotationService = toolBox.getServices().getAnnotationService();
            AnnotationServiceDecorator asd = new AnnotationServiceDecorator(toolBox);
            annotationService.deleteAssociation(createdAssociation.getUuid())
                    .thenAccept(v -> {
                        createdAssociation = null;
                        asd.refreshAnnotationsView(annotation.getObservationUuid());
                    });
        }
    }

    @Override
    public String getDescription() {
        return "Add Association: " + association;
    }
}
