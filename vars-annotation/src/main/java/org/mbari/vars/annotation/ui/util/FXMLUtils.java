package org.mbari.vars.annotation.ui.util;

import javafx.fxml.FXMLLoader;

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
    public static <T> T newInstance(Class<T> clazz, String fxmlPath, ResourceBundle i18n) {
        FXMLLoader loader = new FXMLLoader(clazz.getResource(fxmlPath), i18n);

        try {
            loader.load();
            return loader.getController();
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to load " + fxmlPath, e);
        }
    }


}
