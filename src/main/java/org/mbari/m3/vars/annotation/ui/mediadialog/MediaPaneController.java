/*
 * @(#)MediaPropertiesPaneController.java   2017.05.31 at 04:38:46 PDT
 *
 * Copyright 2011 MBARI
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mbari.m3.vars.annotation.ui.mediadialog;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.m3.vars.annotation.util.FXMLUtils;
import org.mbari.m3.vars.annotation.util.FormatUtils;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.util.JFXUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;

/**
 * @author Brian Schlining
 * @since 2017-05-31T16:38:00
 */
public class MediaPaneController {

    private final Logger log = LoggerFactory.getLogger(getClass());
    @FXML
    private ResourceBundle bundle;

    @FXML
    private TextField camerIdTextField;
    @FXML
    private TextField videoSequenceNameTextField;
    @FXML
    private TextField videoNameTextField;
    @FXML
    private TextField startTimestampTextField;
    @FXML
    private TextField uriTextField;
    @FXML
    private TextField containerTextField;
    @FXML
    private TextField videoSizeTextField;
    @FXML
    private TextField videoDimensionTextField;
    @FXML
    private TextField frameRateTextField;
    @FXML
    private TextField endTimestampTextField;
    @FXML
    private TextField durationTextField;
    @FXML
    private TextField annotationCountTextField;
    @FXML
    private Pane root;

    ObjectProperty<Media> media = new SimpleObjectProperty<>();


    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        mediaProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) resetView();
            else updateView(newValue);
        });
    }

    private void updateView(Media media) {
        if (media == null) {
            camerIdTextField.setText(null);
            videoSequenceNameTextField.setText(null);
            videoNameTextField.setText(null);
            startTimestampTextField.setText(null);
            uriTextField.setText(null);
            uriTextField.setTooltip(null);
            containerTextField.setText(null);
            videoSizeTextField.setText(null);
            videoDimensionTextField.setText(null);
            frameRateTextField.setText(null);
            endTimestampTextField.setText(null);
            durationTextField.setText(null);
            annotationCountTextField.setText(null);
        }
        else {
            camerIdTextField.setText(media.getCameraId());
            videoSequenceNameTextField.setText(media.getVideoSequenceName());
            videoNameTextField.setText(media.getVideoName());
            String st = (media.getStartTimestamp() == null) ? null : media.getStartTimestamp().toString();
            startTimestampTextField.setText(st);
            String uri = (media.getUri() == null) ? null : media.getUri().toString();
            uriTextField.setText(uri);
            uriTextField.setTooltip(new Tooltip(uri));
            containerTextField.setText(media.getContainer());
            String size = (media.getSizeBytes() == null) ? null : FormatUtils.formatSizeBytes(media.getSizeBytes());
            videoSizeTextField.setText(size);
            String dims = (media.getWidth() == null || media.getHeight() == null) ? null :
                    media.getWidth() + " x " + media.getHeight();
            videoDimensionTextField.setText(dims);
            String fr = (media.getFrameRate() == null) ? null : media.getFrameRate().toString();
            frameRateTextField.setText(fr);
            String et = (media.getStartTimestamp() == null || media.getDuration() == null) ? null :
                    media.getStartTimestamp().plus(media.getDuration()).toString();
            endTimestampTextField.setText(et);
            String duration = (media.getDuration() == null) ? null : FormatUtils.formatDuration(media.getDuration());
            durationTextField.setText(duration);
        }
    }

    private void resetView() {
        camerIdTextField.setText(null);
        videoSequenceNameTextField.setText(null);
        videoNameTextField.setText(null);
        startTimestampTextField.setText(null);
        uriTextField.setText(null);
        containerTextField.setText(null);
        videoSizeTextField.setText(null);
        videoDimensionTextField.setText(null);
        frameRateTextField.setText(null);
        endTimestampTextField.setText(null);
        durationTextField.setText(null);
        annotationCountTextField.setText(null);
    }

    public Pane getRoot() {
        return root;
    }


    public Media getMedia() {
        return media.get();
    }

    public ObjectProperty<Media> mediaProperty() {
        return media;
    }


    /**
     * Updates the media value displayed in this dialog. Also makes a call to the
     * annotation database to get a count of annotations to inform annotators
     * if a media has been previously annotated.
     * @param media The media value to view
     * @param annotationService The service used to look up the annotation count
     */
    public void setMedia(Media media, AnnotationService annotationService) {
        Platform.runLater(() -> {
            this.media.set(media);
            if (media != null) {
                annotationService.countAnnotations(media.getVideoReferenceUuid())
                        .thenAccept(ac ->
                            JFXUtilities.runOnFXThread(() -> {
                                annotationCountTextField.setText(ac.getCount() + "");
                            }));
            }
        });
    }



    public static MediaPaneController newInstance() {
        return FXMLUtils.newInstance(MediaPaneController.class, "/fxml/MediaPane.fxml");
    }
}
