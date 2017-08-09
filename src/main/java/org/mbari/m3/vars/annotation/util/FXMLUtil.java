package org.mbari.m3.vars.annotation.util;

import javafx.fxml.FXMLLoader;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.mediaplayers.sharktopoda.SharktopodaPaneController;

import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-08-08T16:03:00
 */
public class FXMLUtil {

    public static <T> T newInstance(Class<T> clazz, String fxmlPath) {
        final ResourceBundle bundle = Initializer.getToolBox().getI18nBundle();
        FXMLLoader loader = new FXMLLoader(clazz.getResource(fxmlPath), bundle);

        try {
            loader.load();
            return loader.getController();
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to load " + fxmlPath, e);
        }
    }
}
