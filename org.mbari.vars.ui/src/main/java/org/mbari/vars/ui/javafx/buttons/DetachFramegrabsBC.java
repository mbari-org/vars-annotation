package org.mbari.vars.ui.javafx.buttons;

import io.reactivex.Observable;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.commands.DetachFramegrabCmd;
import org.mbari.vars.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.ui.events.MediaPlayerChangedEvent;
import org.mbari.vars.ui.javafx.Icons;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.User;
import org.mbari.vars.ui.util.JFXUtilities;

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
//        Text icon = iconFactory.createIcon(MaterialIcon.CLEAR, "30px");
        Text icon = Icons.CLEAR.standardSize();
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
        boolean enable = user != null && 
            selectedAnnotations != null && 
            selectedAnnotations
                .stream()
                .anyMatch(a -> !a.getImages().isEmpty());
        JFXUtilities.runOnFXThread(() -> button.setDisable(!enable));
    }

    @Override
    protected void checkEnable() {
        checkEnable(toolBox.getData().getSelectedAnnotations());
    }

    @Override
    protected void apply() {
        List<Annotation> annotations = new ArrayList<>(toolBox.getData().getSelectedAnnotations());
        toolBox.getEventBus().send(new DetachFramegrabCmd(annotations));
    }
}
