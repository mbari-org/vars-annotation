package org.mbari.m3.vars.annotation;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-05-15T10:52:00
 */
public class Constants {

    private static final Config CONFIG = ConfigFactory.load();

    private static final ResourceBundle I18N_BUNDLE = ResourceBundle.getBundle("i18n",
            Locale.getDefault());

    private static Path settingsDirectory;

    private static final EventBus EVENT_BUS = new EventBus();

    private static final AppState APP_STATE = new AppState(getSettingsDirectory());

    private static final UIToolBox TOOL_BOX = new UIToolBox(APP_STATE, EVENT_BUS, I18N_BUNDLE, CONFIG);

    public static UIToolBox getToolBox() {
        return TOOL_BOX;
    }

    /**
     * The settingsDirectory is scratch space for VARS
     *
     * @return The path to the settings directory. null is returned if the
     *  directory doesn't exist (or can't be created) or is not writable.
     */
    public static Path getSettingsDirectory() {
        if (settingsDirectory == null) {
            String home = System.getProperty("user.home");
            settingsDirectory = Paths.get(home, ".vars");

            // Make sure the directory exists and we can write to it.
            if (!Files.exists(settingsDirectory)) {
                try {
                    Files.createDirectory(settingsDirectory);
                    if (!Files.isWritable(settingsDirectory)) {
                        settingsDirectory = null;
                    }
                }
                catch (IOException e) {
                    String msg = "Unable to create a setting directory at " + settingsDirectory + ".";
                    LoggerFactory.getLogger(Constants.class).error(msg, e);
                    settingsDirectory = null;
                }
            }
        }
        return settingsDirectory;
    }


}
