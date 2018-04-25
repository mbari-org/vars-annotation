package org.mbari.m3.vars.annotation.util;

import javafx.fxml.FXMLLoader;
import org.mbari.m3.vars.annotation.Initializer;

import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-08-08T16:03:00
 */
public class FXMLUtils {

    /**
     *
     * @param clazz The controller class
     * @param fxmlPath The path to use to look up the fxml file for the controller
     * @param <T> The type of the controller class
     * @return A controller loaded from the FXML file. The i18n bundle will be injected into it.
     */
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
