package org.mbari.m3.vars.annotation;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.mbari.m3.vars.annotation.util.LessCSSLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

/**
 * @author Brian Schlining
 * @since 2017-05-15T10:52:00
 */
public class Initializer {

    private static Path settingsDirectory;
    private static Path imageDirectory;
    private static Injector injector;

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
            Services services = getInjector().getInstance(Services.class);
            ResourceBundle bundle = ResourceBundle.getBundle("i18n",
                    Locale.getDefault());

            // We're using less!! Load it using our custom loader
            LessCSSLoader lessLoader = new LessCSSLoader();
            String stylesheet = lessLoader.loadLess(Initializer.class.getResource("/less/annotation.less"))
                    .toExternalForm();
            toolBox = new UIToolBox(new Data(),
                    services,
                    new EventBus(),
                    bundle,
                    getConfig(),
                    Collections.singletonList(stylesheet),
                    new ForkJoinPool());
        }
        return toolBox;
    }

    private static final Logger log = LoggerFactory.getLogger(Initializer.class);

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

    private static Path createDirectory(Path path) {
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

    public static Injector getInjector() {
        if (injector == null) {
            String moduleName = getConfig().getString("app.injector.module.class");
            try {
                Class clazz = Class.forName(moduleName);
                // TODO in java 9 use clazz.getDeclaredConstructor().newInstance()
                // You'll have to find one where constructor.getParameterCount == 0
                Module module = (Module) clazz.newInstance();
                injector = Guice.createInjector(module);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create dependency injector", e);
            }
        }
        return injector;
    }


}
