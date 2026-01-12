package org.mbari.vars.ui.messages;

import javafx.stage.Window;

import java.net.URL;

/**
 * @author Brian Schlining
 * @since 2017-10-26T10:25:00
 */
public class SaveImageMsg implements Message {
    private final URL url;
    private final Window window;

    public SaveImageMsg(URL url, Window owner) {
        this.url = url;
        this.window = owner;
    }

    public URL getUrl() {
        return url;
    }

    public Window getWindow() {
        return window;
    }
}
