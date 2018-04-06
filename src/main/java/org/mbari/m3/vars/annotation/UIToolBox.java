package org.mbari.m3.vars.annotation;


import com.typesafe.config.Config;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.mbari.m3.vars.annotation.mediaplayers.MediaPlayer;
import org.mbari.vcr4j.VideoError;
import org.mbari.vcr4j.VideoState;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * @author Brian Schlining
 * @since 2017-06-10T12:06:00
 */
public class UIToolBox {

    private final ExecutorService executorService;
    private final EventBus eventBus;
    private final ResourceBundle i18nBundle;
    private final Config config;
    private final Data data;
    private final Services services;
    private final ObjectProperty<MediaPlayer<? extends VideoState, ? extends VideoError>> mediaPlayer = new SimpleObjectProperty<>();

    /** URL to the stylesheet used for the apps */
    private final Collection<String> stylesheets;

    @Inject
    public UIToolBox(Data data,
                     Services services,
                     EventBus eventBus,
                     ResourceBundle i18nBundle,
                     Config config,
                     Collection<String> stylesheets,
                     ExecutorService executorService) {
        this.data = data;
        this.services = services;
        this.eventBus = eventBus;
        this.i18nBundle = i18nBundle;
        this.config = config;
        this.stylesheets = Collections.unmodifiableCollection(stylesheets);
        this.executorService = executorService;
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

    public Config getConfig() {
        return config;
    }

    public Data getData() {
        return data;
    }

    public Services getServices() {
        return services;
    }

    public MediaPlayer<? extends VideoState, ? extends VideoError> getMediaPlayer() {
        return mediaPlayer.get();
    }

    public ObjectProperty<MediaPlayer<? extends VideoState, ? extends VideoError>> mediaPlayerProperty() {
        return mediaPlayer;
    }
}
