package org.mbari.vars.ui.javafx.mediadialog;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.services.AnnotationService;
import org.mbari.vars.services.MediaService;

import java.util.ResourceBundle;

/**
 * Dialog for selecting VideoReferences (Media) from the {@link MediaService}
 *
 * @author Brian Schlining
 * @since 2017-06-01T14:11:00
 */
public class SelectMediaDialog extends Dialog<Media> {

    private final AnnotationService annotationService;
    private final MediaService mediaService;
    private final ResourceBundle uiBundle;

    public SelectMediaDialog(AnnotationService annotationService,
            MediaService mediaService,
            ResourceBundle uiBundle) {
        this.annotationService = annotationService;
        this.mediaService = mediaService;
        this.uiBundle = uiBundle;
        VideoBrowserPaneController controller = new VideoBrowserPaneController(annotationService,
                mediaService, uiBundle);
        getDialogPane().setContent(controller.getRoot());
        ButtonType ok = new ButtonType(uiBundle.getString("global.ok"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = new ButtonType(uiBundle.getString("global.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(ok, cancel);

        setResultConverter(buttonType -> {
            if (buttonType == ok) {
                return controller.getSelectedMedia().orElse(null);
            }
            else {
                return null;
            }
        });
    }

}
