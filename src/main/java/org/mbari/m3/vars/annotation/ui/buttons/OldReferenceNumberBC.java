package org.mbari.m3.vars.annotation.ui.buttons;

import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.CreateAssociationsCmd;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.ui.AnnotationServiceDecorator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-09-12T16:43:00
 */
public class OldReferenceNumberBC extends AbstractBC {

    private ChoiceDialog<String> dialog;
    private final String associationKey;
    private final AnnotationServiceDecorator decorator;

    public OldReferenceNumberBC(Button button, UIToolBox toolBox) {
        super(button, toolBox);
        this.associationKey = toolBox.getConfig()
                .getString("app.annotation.identity.reference");
        this.decorator = new AnnotationServiceDecorator(toolBox);
    }

    protected void init() {
        String tooltip = toolBox.getI18nBundle().getString("buttons.oldnumber");
        MaterialIconFactory iconFactory = MaterialIconFactory.get();
        Text icon = iconFactory.createIcon(MaterialIcon.EXPOSURE_NEG_1, "30px");
        initializeButton(tooltip, icon);
    }

    protected void apply() {
        Media media = toolBox.getData().getMedia();
        List<Annotation> selected = new ArrayList<>(toolBox.getData().getSelectedAnnotations());
        List<String> concepts = selected.stream()
                .map(Annotation::getConcept)
                .distinct()
                .collect(Collectors.toList());

        if (concepts.size() == 1) {

            decorator.findReferenceNumberAssociationsForConcept(media, associationKey, concepts.get(0))
                    .thenAccept(as -> {
                        List<String> refNums = associationToIdentityRefs(as);
                        Platform.runLater(() -> {
                            ChoiceDialog<String> dlg = getDialog();
                            dlg.getItems().clear();
                            if (!refNums.isEmpty()) {
                                dlg.getItems().addAll(refNums);
                                dlg.setSelectedItem(refNums.get(refNums.size() - 1));
                                Optional<String> integer = dlg.showAndWait();
                                integer.ifPresent(i -> {
                                    Association a = new Association(associationKey, Association.VALUE_SELF, i);
                                    toolBox.getEventBus()
                                            .send(new CreateAssociationsCmd(a, selected));
                                });
                            }
                        });

                    });
        }
    }

    private ChoiceDialog<String> getDialog() {
        if (dialog == null) {
            ResourceBundle i18n = toolBox.getI18nBundle();
            MaterialIconFactory iconFactory = MaterialIconFactory.get();
            Text icon = iconFactory.createIcon(MaterialIcon.EXPOSURE_NEG_1, "30px");
            dialog = new ChoiceDialog<>();
            dialog.initOwner(toolBox.getPrimaryStage());
            dialog.setTitle(i18n.getString("buttons.oldnumber.dialog.title"));
            dialog.setHeaderText(i18n.getString("buttons.oldnumber.dialog.header"));
            dialog.setContentText(i18n.getString("buttons.oldnumber.dialog.content"));
            dialog.setGraphic(icon);
            dialog.getDialogPane().getStylesheets().addAll(toolBox.getStylesheets());
        }
        return dialog;
    }

    private List<String> associationToIdentityRefs(List<Association> as) {
        return as.stream()
                .map(Association::getLinkValue)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

}
