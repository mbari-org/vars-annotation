package org.mbari.m3.vars.annotation.ui.mediadialog;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;
import org.mbari.m3.vars.annotation.model.Media;

import java.time.Duration;

/**
 * @author Brian Schlining
 * @since 2017-05-31T15:01:00
 */
public class MediaPaneController {

    private ObjectProperty<Media> mediaProperty = new SimpleObjectProperty<>();
    private ObjectProperty<PropertySheet> propertySheetProperty = new SimpleObjectProperty<>();
    private final Pane root = new Pane();


    public MediaPaneController() {

        // When a new Media is set we need to create a new property sheet and set it in it's property
        Binding<PropertySheet> binding = Bindings.createObjectBinding(() -> {
            if (mediaProperty.get() != null) {
                ObservableList<PropertySheet.Item> props = BeanPropertyUtils.getProperties(new MiniMedia(mediaProperty.get()));
                return new PropertySheet(props);
            }
            else {
                return null;
            }
        }, mediaProperty);
        propertySheetProperty.bind(binding);

        // When a property sheet changes, discard the old one and add the new one to root
        propertySheetProperty.addListener((obs, oldValue, newValue) -> {
            if (oldValue != null) {
                root.getChildren().remove(oldValue);
            }
            if (newValue != null) {
                root.getChildren().add(newValue);
            }
        });
    }

    public Pane getRoot() {
        return root;
    }

    public Media getMedia() {
        return mediaProperty.get();
    }

    public ObjectProperty<Media> mediaProperty() {
        return mediaProperty;
    }

    public void setMedia(Media media) {
        mediaProperty.set(media);
    }

    /**
     * Facade over media the handles help handle nulls and makes things pretty.
     */
    class MiniMedia {
        private final Media media;
        String cameraId;


        MiniMedia(Media media) {
            this.media = media;
        }

        String getCameraId() {
            return (media.getCameraId() == null) ? "" : media.getCameraId();
        }

        void setCameraId(String id) {}

        String getContainer() {
            return (media.getContainer() == null) ? "" : media.getContainer();
        }

        String getDescription() {
            return (media.getDescription() == null) ? "" : media.getDescription();
        }

        String getDuration() {
            return (media.getDuration() == null) ? "" : formatDuration(media.getDuration());
        }

        Double getFrameRate() {
            return (media.getFrameRate() == null) ? 0D : media.getFrameRate();
        }

        String getSize() {
            return (media.getSizeBytes() == null) ? "" : formatSizeBytes(media.getSizeBytes());
        }

        String getStart() {
            return (media.getStartTimestamp() == null) ? "" : media.getStartTimestamp().toString();
        }

        String getUri() {
            return (media.getUri() == null) ? "" : media.getUri().toString();
        }

        String getVideoName() {
            return (media.getVideoName() == null) ? "" : media.getVideoName();
        }

        String getVideoSequenceName() {
            return (media.getVideoSequenceName() == null) ? "" : media.getVideoSequenceName();
        }

        Integer getPixelWidth() {
            return (media.getWidth() == null) ? 0 : media.getWidth();
        }

        Integer getPixelHeight() {
            return (media.getHeight() == null) ? 0 : media.getHeight();
        }

        String formatSizeBytes(Long bytes) {
            Double gb = bytes * 1e-9;
            if (gb > 1) {
                return String.format("%.2f GB", gb);
            }
            else {
                Double mb = bytes * 1e-6;
                return String.format(".2f MB", mb);
            }
        }

        String formatDuration(Duration duration) {
            long seconds = duration.getSeconds();
            long absSeconds = Math.abs(seconds);
            String positive = String.format(
                    "%d:%02d:%02d",
                    absSeconds / 3600,
                    (absSeconds % 3600) / 60,
                    absSeconds % 60);
            return seconds < 0 ? "-" + positive : positive;
        }
    }

}
