package org.mbari.vars.ui.javafx.buttons;

import javafx.collections.ListChangeListener;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.commands.CreateAssociationsCmd;
import org.mbari.vars.ui.javafx.Icons;
import org.mbari.vars.ui.mediaplayers.sharktopoda2.LocalizedAnnotation;
import org.mbari.vcr4j.remote.control.commands.localization.Localization;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NewBoundingBoxBC extends AbstractBC {

    public NewBoundingBoxBC(Button button, UIToolBox toolBox) {
        super(button, toolBox);
    }

    @Override
    protected void apply() {
        var selectedAnnotations = new ArrayList<>(toolBox.getData().getSelectedAnnotations());
        if (selectedAnnotations.size() == 1) {
            var annotation = selectedAnnotations.get(0);
            var media = toolBox.getData().getMedia();
            if (canLocalize(media, annotation)) {
                var association = toAssociation(annotation, media);
                association.resetUuid();
                toolBox.getEventBus().send(new CreateAssociationsCmd(association, List.of(annotation)));
            }
        }
    }

    private boolean canLocalize(Media media, Annotation annotation) {
        return media != null
                && media.getWidth() != null
                && media.getHeight() != null
                && annotation.getElapsedTime() != null
                && annotation.getVideoReferenceUuid().equals(media.getVideoReferenceUuid());
    }

    private Association toAssociation(Annotation annotation, Media media) {
        var w = media.getWidth();
        var h = media.getHeight();
        int width = (int) Math.round(w * 0.1);
        int height = (int) Math.round(h * 0.1);
        int x = (int) Math.round((w / 2.0) - (width / 2.0));
        int y = (int) Math.round((h / 2.0) - (height / 2.0));
        var duration = annotation.getDuration() == null ? null : annotation.getDuration().toMillis();
        var localization = new Localization(UUID.randomUUID(),
                annotation.getConcept(),
                annotation.getElapsedTime().toMillis(),
                duration,
                x, y, width, height, null);
        return LocalizedAnnotation.toAssociation(localization);
    }

    @Override
    protected void init() {
        String tooltip = toolBox.getI18nBundle().getString("buttons.boundingbox");
        Text icon = Icons.FORMAT_SHAPES.standardSize();
        initializeButton(tooltip, icon);
        var selectedAnnotations = toolBox.getData().getSelectedAnnotations();
        selectedAnnotations
                .addListener((ListChangeListener<Annotation>) c -> {
                    var disable = true;
                    if (selectedAnnotations.size() == 1) {
                        var head = selectedAnnotations.get(0);
                        var media = toolBox.getData().getMedia();
                        disable = !canLocalize(media, head);
                    }
                    button.setDisable(disable);
                });
    }
}
