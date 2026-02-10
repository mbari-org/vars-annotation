package org.mbari.vars.annotation.ui;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.mbari.vars.annotation.etc.jdk.crypto.AES;
import org.mbari.vars.annotation.etc.rxjava.EventBus;
import org.mbari.vars.annotation.services.Services;
import org.mbari.vars.annotation.services.VarsServiceFactory;
import org.mbari.vars.annotation.ui.mediaplayers.sharktopoda.SharktopodaSettingsPaneController;
import org.mbari.vars.annotation.etc.jdk.Loggers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

/**
 * JPMS didn't play well with dependency injection via Guice. I ripped it out
 * and am using this static intializer class to wire together all the services
 * and factories.
 * @author Brian Schlining
 * @since 2017-05-15T10:52:00
 */
public class Initializer {

    private static final Loggers log = new Loggers(Initializer.class);
    private static final byte[] lock = {0};

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

    public static Services loadServices() {
        var serviceFactory = new VarsServiceFactory(getConfig());
        return serviceFactory.newServices();
    }


    public static UIToolBox getToolBox() {
        if (toolBox == null) {
            synchronized (lock) {
//                Services services = ServicesBuilder.build(Initializer.getConfig());
                Services services = loadServices();
                ResourceBundle bundle = ResourceBundle.getBundle("i18n",
                        Locale.getDefault());

                String stylesheet = Initializer.class.getResource("/css/annotation.css").toExternalForm();

                Data data = new Data();
                Integer timeJump = SharktopodaSettingsPaneController.getTimeJump();
                log.atInfo().log("Setting Time Jump to " + timeJump + " millis");
                data.setTimeJump(timeJump);

                toolBox = new UIToolBox(data,
                        services,
                        new EventBus(),
                        bundle,
                        getConfig(),
                        Collections.singletonList(stylesheet),
                        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2),
                        getAes());
            }
        }
        return toolBox;
    }

    public static AES getAes() {
        return new AES("brian@mbari.org 1993-08-21");
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
                log.atWarn().log("Failed to create settings directory at " + path);
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
                    log.atWarn().log("Failed to create image directory at " + path);
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
                log.atError().withCause(e).log(msg);
                createdPath = null;
            }
        }
        return createdPath;
    }

}
