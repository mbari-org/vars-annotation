package org.mbari.vars.ui;


import com.typesafe.config.Config;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.stage.Stage;
import org.mbari.vars.core.EventBus;
import org.mbari.vars.core.crypto.AES;
import org.mbari.vars.ui.mediaplayers.MediaPlayer;
import org.mbari.vars.services.Services;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;

import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

/**
 * One-stop shop for Shared resources that are used throughout the VARS
 * Annotation application.
 *
 * @author Brian Schlining
 * @since 2017-06-10T12:06:00
 */
public class UIToolBox {

    private final ExecutorService executorService;
    private final EventBus eventBus;
    private final ResourceBundle i18nBundle;
    private final Config config;
    private final AppConfig appConfig;
    private final Data data;
    private Services services;
    private final ObjectProperty<MediaPlayer<? extends VideoState, ? extends VideoError>> mediaPlayer = new SimpleObjectProperty<>();
    private final ObjectProperty<Stage> primaryStage = new SimpleObjectProperty<>();
    private final AES aes;

    /** URL to the stylesheet used for the apps */
    private final Collection<String> stylesheets;

    public UIToolBox(Data data,
                     Services services,
                     EventBus eventBus,
                     ResourceBundle i18nBundle,
                     Config config,
                     Collection<String> stylesheets,
                     ExecutorService executorService,
                     AES aes) {
        this.data = data;
        this.services = services;
        this.eventBus = eventBus;
        this.i18nBundle = i18nBundle;
        this.config = config;
        this.appConfig = new AppConfig(config);
        this.stylesheets = Collections.unmodifiableCollection(stylesheets);
        this.executorService = executorService;
        this.aes = aes;
    }

    public AES getAes() {
        return aes;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public Collection<String> getStylesheets() {
        return stylesheets;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public ResourceBundle getI18nBundle() {
        return i18nBundle;
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    public Config getConfig() {
        return config;
    }

    public Data getData() {
        return data;
    }

    public Services getServices() {
        return services;
    }

    public void setServices(Services services) {
        this.services = services;
    }

    public MediaPlayer<? extends VideoState, ? extends VideoError> getMediaPlayer() {
        return mediaPlayer.get();
    }

    public ObjectProperty<MediaPlayer<? extends VideoState, ? extends VideoError>> mediaPlayerProperty() {
        return mediaPlayer;
    }

    public Stage getPrimaryStage() {
        return primaryStage.get();
    }

    public ObjectProperty<Stage> primaryStageProperty() {
        return primaryStage;
    }
}
