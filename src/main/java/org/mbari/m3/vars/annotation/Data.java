package org.mbari.m3.vars.annotation;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @author Brian Schlining
 * @since 2017-05-11T13:07:00
 */
public class Data {

    private ObjectProperty<Media> media = new SimpleObjectProperty<>();

    private ObservableList<Annotation> annotations = FXCollections.emptyObservableList();

    private ObservableList<Annotation> selectedAnnotations = FXCollections.emptyObservableList();

    private ObjectProperty<User> user  = new SimpleObjectProperty<>();

    private StringProperty activity = new SimpleStringProperty();

    private StringProperty group = new SimpleStringProperty();

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

    public void setAnnotations(Collection<Annotation> annotations) {
        this.annotations.clear();
        this.annotations.addAll(annotations);
    }

    public User getUser() {
        return user.get();
    }

    public ObjectProperty<User> userProperty() {
        return user;
    }

    public void setUser(User user) {
        this.user.set(user);
    }

    public ObservableList<Annotation> getSelectedAnnotations() {
        return selectedAnnotations;
    }

    public void setSelectedAnnotations(Collection<Annotation> selectedAnnotations) {
        this.selectedAnnotations.clear();
        this.selectedAnnotations.addAll(selectedAnnotations);
    }

    public String getActivity() {
        return activity.get();
    }

    public StringProperty activityProperty() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity.set(activity);
    }

    public String getGroup() {
        return group.get();
    }

    public StringProperty groupProperty() {
        return group;
    }

    public void setGroup(String group) {
        this.group.set(group);
    }
}
