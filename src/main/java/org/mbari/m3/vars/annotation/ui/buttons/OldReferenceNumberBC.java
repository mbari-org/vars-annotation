package org.mbari.m3.vars.annotation.ui.buttons;

import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import io.reactivex.Observable;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.CreateAssociationsCmd;
import org.mbari.m3.vars.annotation.events.MediaChangedEvent;
import org.mbari.m3.vars.annotation.events.UserChangedEvent;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Association;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.model.User;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Brian Schlining
 * @since 2017-09-12T16:43:00
 */
public class OldReferenceNumberBC {

    private final ChoiceDialog<Integer> dialog = new ChoiceDialog<>();
    private final Button button;
    private final UIToolBox toolBox;
    private final String associationKey;

    public OldReferenceNumberBC(Button button, UIToolBox toolBox) {
        this.button = button;
        this.toolBox = toolBox;
        this.associationKey = toolBox.getConfig()
                .getString("app.annotation.identity.reference");
        init();
    }

    protected void init() {

        button.setTooltip(new Tooltip(toolBox.getI18nBundle().getString("buttons.oldnumber")));
        MaterialIconFactory iconFactory = MaterialIconFactory.get();
        Text icon = iconFactory.createIcon(MaterialIcon.EXPOSURE_NEG_1, "30px");
        button.setText(null);
        button.setGraphic(icon);
        button.setDisable(true);
        button.setOnAction(e -> apply());

        ResourceBundle i18n = toolBox.getI18nBundle();
        dialog.setTitle(i18n.getString("buttons.oldnumber.dialog.title"));
        dialog.setHeaderText(i18n.getString("buttons.oldnumber.dialog.header"));
        dialog.setContentText(i18n.getString("buttons.oldnumber.dialog.content"));
        dialog.setGraphic(icon);
        dialog.getDialogPane().getStylesheets().addAll(toolBox.getStylesheets());

        Observable<Object> observable = toolBox.getEventBus().toObserverable();
        observable.ofType(MediaChangedEvent.class)
                .subscribe(m -> checkEnable());
        observable.ofType(UserChangedEvent.class)
                .subscribe(m -> checkEnable());

    }

    private void apply() {
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

    private void checkEnable() {
        Media media = toolBox.getData().getMedia();
        User user = toolBox.getData().getUser();
        boolean enable =  media != null && user != null;
        button.setDisable(!enable);
    }
}
