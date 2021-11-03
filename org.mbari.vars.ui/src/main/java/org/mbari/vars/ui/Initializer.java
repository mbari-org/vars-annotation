package org.mbari.vars.ui;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.services.ServicesBuilder;
import org.mbari.vars.services.Services;
import org.mbari.vars.core.util.LessCSSLoader;
import org.mbari.vars.ui.mediaplayers.sharktopoda.SharktopodaSettingsPaneController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

/**
 * JPMS didn't play well with dependency injection via Guice. I ripped it out
 * and am using this static intializer class to wire together all the services
 * and factories.
 * @author Brian Schlining
 * @since 2017-05-15T10:52:00
 */
public class Initializer {

    private static final Logger log = LoggerFactory.getLogger(Initializer.class);

    private static Path settingsDirectory;
    private static Path imageDirectory;

    private static UIToolBox toolBox;

    private static Config config;

    /**
     * First looks for the file `~/.vars/vars-annotation.conf` and, if found,
     * loads that file. Otherwise used the usual `reference.conf`/`application.conf`
     * combination for typesafe's config library.
     * @return
     */
    public static Config getConfig() {
        if (config == null) {
            Config defaultConfig =  ConfigFactory.load();
            final Path p0 = getSettingsDirectory();
            final Path path = p0.resolve("vars-annotation.conf");
            if (Files.exists(path)) {
                config = ConfigFactory.parseFile(path.toFile())
                        .withFallback(defaultConfig);
            }
            else {
                config = defaultConfig;
            }
        }
        return config;
    }


    public static UIToolBox getToolBox() {
        if (toolBox == null) {
            Services services = ServicesBuilder.build(Initializer.getConfig());
            ResourceBundle bundle = ResourceBundle.getBundle("i18n",
                    Locale.getDefault());

            // We're using less!! Load it using our custom loader
            // LessCSSLoader lessLoader = new LessCSSLoader();
            // String stylesheet = lessLoader.loadLess(Initializer.class.getResource("/less/annotation.less"))
            //         .toExternalForm();
            String stylesheet = Initializer.class.getResource("/css/annotation.css").toExternalForm();

            //
            Data data = new Data();
            Integer timeJump = SharktopodaSettingsPaneController.getTimeJump();
            log.info("Setting Time Jump to {} millis", timeJump);
            data.setTimeJump(timeJump);

            toolBox = new UIToolBox(data,
                    services,
                    new EventBus(),
                    bundle,
                    getConfig(),
                    Collections.singletonList(stylesheet),
                    new ForkJoinPool());
        }
        return toolBox;
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
            Path path = Paths.get(home, ".vars");
            settingsDirectory = createDirectory(path);
            if (settingsDirectory == null) {
                log.warn("Failed to create settings directory at " + path);
            }
        }
        return settingsDirectory;
    }

    public static Path getImageDirectory() {
        if (imageDirectory == null) {
            Path settingsDir = getSettingsDirectory();
            if (settingsDir != null) {
                Path path = Paths.get(settingsDir.toString(), "images");
                imageDirectory = createDirectory(path);
                if (imageDirectory == null) {
                    log.warn("Failed to create image directory at " + path);
                }
            }

        }
        return imageDirectory;
    }

    public static Path createDirectory(Path path) {
        Path createdPath = path;
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
                if (!Files.isWritable(path)) {
                    createdPath = null;
                }
            }
            catch (IOException e) {
                String msg = "Unable to create a directory at " + path + ".";
                LoggerFactory.getLogger(Initializer.class).error(msg, e);
                createdPath = null;
            }
        }
        return createdPath;
    }

}
