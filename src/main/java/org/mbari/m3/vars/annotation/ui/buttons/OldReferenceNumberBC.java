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

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-09-12T16:43:00
 */
public class OldReferenceNumberBC extends AbstractBC {

    private ChoiceDialog<Integer> dialog;
    private final String associationKey;

    public OldReferenceNumberBC(Button button, UIToolBox toolBox) {
        super(button, toolBox);
        this.associationKey = toolBox.getConfig()
                .getString("app.annotation.identity.reference");
    }

    protected void init() {

        dialog = new ChoiceDialog<>();

        ResourceBundle i18n = toolBox.getI18nBundle();
        String tooltip = i18n.getString("buttons.oldnumber");
        MaterialIconFactory iconFactory = MaterialIconFactory.get();
        Text icon = iconFactory.createIcon(MaterialIcon.EXPOSURE_NEG_1, "30px");
        initializeButton(tooltip, icon);

        dialog.setTitle(i18n.getString("buttons.oldnumber.dialog.title"));
        dialog.setHeaderText(i18n.getString("buttons.oldnumber.dialog.header"));
        dialog.setContentText(i18n.getString("buttons.oldnumber.dialog.content"));
        dialog.setGraphic(icon);
        dialog.getDialogPane().getStylesheets().addAll(toolBox.getStylesheets());

    }

    protected void apply() {
        Media media = toolBox.getData().getMedia();
        UUID videoReferenceUuid = media.getVideoReferenceUuid();
        List<Annotation> selected = new ArrayList<>(toolBox.getData().getSelectedAnnotations());
        toolBox.getServices()
                .getAnnotationService()
                .findByVideoReferenceAndLinkName(videoReferenceUuid, associationKey)
                .thenAccept(as -> {

                    List<Integer> refNums = associationsToRefNumbers(as);
                    Platform.runLater(() -> {
                        dialog.getItems().clear();
                        if (!refNums.isEmpty()) {
                            dialog.getItems().addAll(refNums);
                            dialog.setSelectedItem(refNums.iterator().next());
                            Optional<Integer> integer = dialog.showAndWait();
                            integer.ifPresent(i -> {
                                Association a = new Association(associationKey, Association.VALUE_SELF, i + "");
                                toolBox.getEventBus()
                                        .send(new CreateAssociationsCmd(a, selected));
                            });
                        }
                    });

                });
    }

    private List<Integer> associationsToRefNumbers(List<Association> as) {
        return as.stream()
                .map(ass -> {
                   try {
                        return Integer.parseInt(ass.getLinkValue());
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .distinct()
                .sorted(Comparator.reverseOrder()) //reverse sort
                .collect(Collectors.toList());
    }

}
