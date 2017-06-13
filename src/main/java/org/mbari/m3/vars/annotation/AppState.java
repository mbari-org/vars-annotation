package org.mbari.m3.vars.annotation;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Authorization;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * @author Brian Schlining
 * @since 2017-05-11T13:07:00
 */
public class AppState {

    private ObjectProperty<Media> media = new SimpleObjectProperty<>();

    private ObservableList<Annotation> annotations = FXCollections.emptyObservableList();

    private ObjectProperty<ConceptService> conceptService = new SimpleObjectProperty<>();

    private ObjectProperty<AuthService> authService = new SimpleObjectProperty<>();

    /**
     * This stores the clientSecret need to start JWT handshake.
     * TODO on change this should recreate annotationService, associationService, imageservice
     */
    private ObjectProperty<Authorization> annoAuthorization = new SimpleObjectProperty<>();

    private ObjectProperty<AnnotationService> annotationService = new SimpleObjectProperty<>();

    private ObjectProperty<Authorization> mediaAuthorization = new SimpleObjectProperty<>();

    private ObjectProperty<MediaService> mediaService = new SimpleObjectProperty<>();

    private ObjectProperty<Authorization> accountsAuthorization = new SimpleObjectProperty<>();

    private ObjectProperty<PreferencesService> preferencesService = new SimpleObjectProperty<>();

    private ObjectProperty<UserService> userService = new SimpleObjectProperty<>();

    private final Path settingsDirectory;

    public AppState(Path settingsDirectory) {
        this.settingsDirectory = settingsDirectory;
    }

    public Media getMedia() {
        return media.get();
    }

    public ObjectProperty<Media> mediaProperty() {
        return media;
    }

    public void setMedia(Media media) {
        this.media.set(media);
    }

    public ObservableList<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(ObservableList<Annotation> annotations) {
        this.annotations = annotations;
    }

    public ConceptService getConceptService() {
        return conceptService.get();
    }

    public ObjectProperty<ConceptService> conceptServiceProperty() {
        return conceptService;
    }

    public void setConceptService(ConceptService conceptService) {
        this.conceptService.set(conceptService);
    }

    public AuthService getAuthService() {
        return authService.get();
    }

    public ObjectProperty<AuthService> authServiceProperty() {
        return authService;
    }

    public void setAuthService(AuthService authService) {
        this.authService.set(authService);
    }

    public Authorization getAnnoAuthorization() {
        return annoAuthorization.get();
    }

    public ObjectProperty<Authorization> annoAuthorizationProperty() {
        return annoAuthorization;
    }

    public void setAnnoAuthorization(Authorization annoAuthorization) {
        this.annoAuthorization.set(annoAuthorization);
    }

    public AnnotationService getAnnotationService() {
        return annotationService.get();
    }

    public ObjectProperty<AnnotationService> annotationServiceProperty() {
        return annotationService;
    }

    public void setAnnotationService(AnnotationService annotationService) {
        this.annotationService.set(annotationService);
    }

    public Authorization getMediaAuthorization() {
        return mediaAuthorization.get();
    }

    public ObjectProperty<Authorization> mediaAuthorizationProperty() {
        return mediaAuthorization;
    }

    public void setMediaAuthorization(Authorization mediaAuthorization) {
        this.mediaAuthorization.set(mediaAuthorization);
    }

    public MediaService getMediaService() {
        return mediaService.get();
    }

    public ObjectProperty<MediaService> mediaServiceProperty() {
        return mediaService;
    }

    public void setMediaService(MediaService mediaService) {
        this.mediaService.set(mediaService);
    }

    public Authorization getAccountsAuthorization() {
        return accountsAuthorization.get();
    }

    public ObjectProperty<Authorization> accountsAuthorizationProperty() {
        return accountsAuthorization;
    }

    public void setAccountsAuthorization(Authorization accountsAuthorization) {
        this.accountsAuthorization.set(accountsAuthorization);
    }

    public PreferencesService getPreferencesService() {
        return preferencesService.get();
    }

    public ObjectProperty<PreferencesService> preferencesServiceProperty() {
        return preferencesService;
    }

    public void setPreferencesService(PreferencesService preferencesService) {
        this.preferencesService.set(preferencesService);
    }

    public UserService getUserService() {
        return userService.get();
    }

    public ObjectProperty<UserService> userServiceProperty() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService.set(userService);
    }

    public Path getSettingsDirectory() {
        return settingsDirectory;
    }
}
