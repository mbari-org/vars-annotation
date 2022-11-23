package org.mbari.vars.ui;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.services.model.User;

import java.util.Collection;

/**
 * `Global` state is kept in this object. The current Data object can be retrieved from the
 * UIToolbox instance.
 * @author Brian Schlining
 * @since 2017-05-11T13:07:00
 */
public class  Data {

    private final ObjectProperty<Media> media = new SimpleObjectProperty<>();

    private final BooleanProperty showCurrentGroupOnly = new SimpleBooleanProperty(false);

    private final ObservableList<Annotation> annotations = FXCollections.observableArrayList();

    private final ObservableList<Annotation> selectedAnnotations = FXCollections.observableArrayList();

    private final ObjectProperty<User> user  = new SimpleObjectProperty<>();

    private final StringProperty activity = new SimpleStringProperty();

    private final StringProperty group = new SimpleStringProperty();

    private final BooleanProperty showConcurrentAnnotations = new SimpleBooleanProperty(false);

    private final BooleanProperty showJsonAssociations = new SimpleBooleanProperty(false);

    private final IntegerProperty timeJump = new SimpleIntegerProperty(1000);

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
        synchronized (this.annotations) {
            Platform.runLater(() -> {
                this.annotations.clear();
                this.annotations.addAll(annotations);
            });
        }
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
        synchronized (this.selectedAnnotations) {
            Platform.runLater(() -> {
                this.selectedAnnotations.clear();
                this.selectedAnnotations.addAll(selectedAnnotations);
            });
        }
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

    public boolean isShowConcurrentAnnotations() {
        return showConcurrentAnnotations.get();
    }

    public BooleanProperty showConcurrentAnnotationsProperty() {
        return showConcurrentAnnotations;
    }

    public void setShowConcurrentAnnotations(boolean showConcurrentAnnotations) {
        this.showConcurrentAnnotations.set(showConcurrentAnnotations);
    }

    public boolean isShowJsonAssociations() {
        return showJsonAssociations.get();
    }

    public BooleanProperty showJsonAssociationsProperty() {
        return showJsonAssociations;
    }

    public void setShowJsonAssociations(boolean showJsonAssociations) {
        this.showJsonAssociations.set(showJsonAssociations);
    }

    public boolean isShowCurrentGroupOnly() {
        return showCurrentGroupOnly.get();
    }

    public BooleanProperty showCurrentGroupOnlyProperty() {
        return showCurrentGroupOnly;
    }

    public void setShowCurrentGroupOnly(boolean showCurrentGroupOnly) {
        this.showCurrentGroupOnly.set(showCurrentGroupOnly);
    }

    public int getTimeJump() {
        return timeJump.get();
    }

    /**
     * The amount of time in milliseconds to jump forward or back on a keystroke
     * @return
     */
    public IntegerProperty timeJumpProperty() {
        return timeJump;
    }

    public void setTimeJump(int timeJump) {
        this.timeJump.set(timeJump);
    }
}
