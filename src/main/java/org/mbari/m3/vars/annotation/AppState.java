package org.mbari.m3.vars.annotation;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Authorization;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.m3.vars.annotation.services.AuthService;
import org.mbari.m3.vars.annotation.services.ConceptService;
import org.mbari.m3.vars.annotation.services.varskbserver.v1.KBConceptService;

import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-05-11T13:07:00
 */
public class AppState {

    private ObjectProperty<Media> mediaProperty = new SimpleObjectProperty<>();

    private ObservableList<Annotation> annotationsProperty = FXCollections.emptyObservableList();

    private ObjectProperty<ConceptService> conceptServiceProperty = new SimpleObjectProperty<>();

    private ObjectProperty<AuthService> authServiceObjectProperty = new SimpleObjectProperty<>();

    private ObjectProperty<ConceptService> conceptServiceObjectProperty = new SimpleObjectProperty<>();

    /**
     * This stores the clientSecret need to start JWT handshake.
     * TODO on change this should recreate annotationService, associationService, imageservice
     */
    private ObjectProperty<Authorization> annoAuthObjectProperty = new SimpleObjectProperty<>();

    private ObjectProperty<AnnotationService> annotationServiceObjectProperty = new SimpleObjectProperty<>();


}
