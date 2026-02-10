package org.mbari.vars.annotation.ui.javafx.buttons;

import io.reactivex.rxjava3.core.Observable;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.events.MediaChangedEvent;
import org.mbari.vars.annotation.ui.events.UserChangedEvent;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vars.oni.sdk.r1.models.User;
import org.mbari.vars.annotation.ui.util.JFXUtilities;

/**
 * @author Brian Schlining
 * @since 2017-09-13T10:42:00
 */
public abstract class AbstractBC {

    protected final Button button;
    protected final UIToolBox toolBox;

    public AbstractBC(Button button, UIToolBox toolBox) {
        this.button = button;
        this.toolBox = toolBox;
        Platform.runLater(this::init);
    }

    public Button getButton() {
        return button;
    }

    protected void initializeButton(String tooltip, Node icon) {
        button.setTooltip(new Tooltip(tooltip));
        button.setText(null);
        button.setGraphic(icon);
        button.setDisable(true);
        button.setOnAction(e -> apply());

        Observable<Object> observable = toolBox.getEventBus().toObserverable();
        observable.ofType(MediaChangedEvent.class)
                .subscribe(m -> checkEnable());
        observable.ofType(UserChangedEvent.class)
                .subscribe(m -> checkEnable());

        checkEnable();
    }


    protected void checkEnable() {
        Media media = toolBox.getData().getMedia();
        User user = toolBox.getData().getUser();
        boolean enable =  media != null && user != null;
        JFXUtilities.runOnFXThread(() -> button.setDisable(!enable));
    }

    protected abstract void apply();

    /**
     * This method should call initializeButton(String tooltip, Node icon)
     */
    protected abstract void init();
}
