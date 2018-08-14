package org.mbari.m3.vars.annotation.ui.buttons;

import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.utils.MaterialIconFactory;
import io.reactivex.Observable;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.commands.DetachFramegrabCmd;
import org.mbari.m3.vars.annotation.events.AnnotationsSelectedEvent;
import org.mbari.m3.vars.annotation.events.MediaPlayerChangedEvent;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.ImageReference;
import org.mbari.m3.vars.annotation.model.User;
import org.mbari.m3.vars.annotation.util.JFXUtilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Brian Schlining
 * @since 2018-08-10T11:22:00
 */
public class DetachFramegrabsBC extends AbstractBC {

    public DetachFramegrabsBC(Button button, UIToolBox toolBox) {
        super(button, toolBox);
    }

    @Override
    protected void init() {
        String tooltip = toolBox.getI18nBundle().getString("buttons.detachimage");
        MaterialIconFactory iconFactory = MaterialIconFactory.get();
        Text icon = iconFactory.createIcon(MaterialIcon.CLEAR, "30px");
        initializeButton(tooltip, icon);

        Observable<Object> observable = toolBox.getEventBus().toObserverable();
        observable.ofType(AnnotationsSelectedEvent.class)
                .subscribe(m -> checkEnable(m.get()));
        observable.ofType(MediaPlayerChangedEvent.class)
                .subscribe(m -> checkEnable());

        // Listen for things other than the button for trigger
        observable.ofType(DetachFramegrabsBC.class)
                .subscribe(m -> apply());

    }

    private void checkEnable(Collection<Annotation> selectedAnnotations) {
        User user = toolBox.getData().getUser();
        boolean enable = user != null && selectedAnnotations
                .stream()
                .anyMatch(a -> !a.getImages().isEmpty());
        JFXUtilities.runOnFXThread(() -> button.setDisable(!enable));
    }

    @Override
    protected void checkEnable() {
        User user = toolBox.getData().getUser();
        boolean enable = user != null && toolBox.getData()
                .getSelectedAnnotations()
                .stream()
                .anyMatch(a -> !a.getImages().isEmpty());
        JFXUtilities.runOnFXThread(() -> button.setDisable(!enable));

    }

    @Override
    protected void apply() {
        List<Annotation> annotations = new ArrayList<>(toolBox.getData().getSelectedAnnotations());
        toolBox.getEventBus().send(new DetachFramegrabCmd(annotations));
    }
}
