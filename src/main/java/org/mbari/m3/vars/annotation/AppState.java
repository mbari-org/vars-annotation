package org.mbari.m3.vars.annotation;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.ConceptService;

import java.util.List;

/**
 * @author Brian Schlining
 * @since 2017-05-11T13:07:00
 */
public class AppState {

    private ObjectProperty<Media> mediaProperty = new SimpleObjectProperty<>();

    private ObservableList<Annotation> annotationsProperty = FXCollections.emptyObservableList();

    private ObjectProperty<ConceptService> conceptServiceProperty = new SimpleObjectProperty<>();




}
