package org.mbari.m3.vars.annotation;

import javafx.scene.Scene;

/**
 * @author Brian Schlining
 * @since 2017-05-10T09:55:00
 */
public class AppController {
    private Scene scene;
    private final UIToolBox uiToolBox;

    public AppController(UIToolBox uiToolBox) {
        this.uiToolBox = uiToolBox;
    }

    public Scene getScene() {
        if (scene == null) {
            //scene = new Scene();

        }
        return scene;

    }
}
