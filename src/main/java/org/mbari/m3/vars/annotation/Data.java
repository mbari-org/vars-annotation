package org.mbari.m3.vars.annotation;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.model.User;

import java.util.Collection;


/**
 * @author Brian Schlining
 * @since 2017-05-11T13:07:00
 */
public class Data {

    private ObservableList<Media> media = FXCollections.emptyObservableList();

    private ObservableList<Annotation> annotations = FXCollections.emptyObservableList();

    private ObjectProperty<User> user  = new SimpleObjectProperty<>();

    public ObservableList<Media> getMedia() {
        return media;
    }

    public void setMedia(Collection<Media> media) {
        this.media.clear();
        this.media.addAll(media);
    }

    public ObservableList<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Collection<Annotation> annotations) {
        this.annotations.clear();
        this.annotations.addAll(annotations);
    }

    public void setMedia(ObservableList<Media> media) {
        this.media = media;
    }

    public void setAnnotations(ObservableList<Annotation> annotations) {
        this.annotations = annotations;
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
}
