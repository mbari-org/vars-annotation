package org.mbari.m3.vars.annotation.ui.buttons;

import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.CreateAssociationsCmd;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.services.CachedReferenceNumberDecorator;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-09-12T16:43:00
 */
public class OldReferenceNumberBC extends AbstractBC {

    private ChoiceDialog<String> dialog;
    private final String associationKey;
    private final CachedReferenceNumberDecorator decorator;

    private final Function<String, Optional<Integer>> toIntMaybe = (a) -> {
        try {
            return Optional.of(Integer.parseInt(a));
        }
        catch (Exception e) {
            return Optional.empty();
        }
    };

    private final Comparator<String> intMaybeComparator = (a, b) -> {
        Optional<Integer> as = toIntMaybe.apply(a);
        Optional<Integer> bs = toIntMaybe.apply(b);
        if (as.isPresent() && bs.isPresent()) {
            return as.get().compareTo(bs.get());
        }
        else {
            return a.compareTo(b);
        }
    };


    public OldReferenceNumberBC(Button button,
                                UIToolBox toolBox,
                                CachedReferenceNumberDecorator decorator) {
        super(button, toolBox);
        this.associationKey = toolBox.getConfig()
                .getString("app.annotation.identity.reference");
        this.decorator = decorator;
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
            decorator.findOldReferenceNumbers(media, concepts.get(0))
                    .thenAccept(as -> updateUI(selected, associationToIdentityRefs(as)));
        }
    }

    private void updateUI(List<Annotation> selected, List<String> refNums) {
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
                .sorted(intMaybeComparator)
                .collect(Collectors.toList());
    }

}
