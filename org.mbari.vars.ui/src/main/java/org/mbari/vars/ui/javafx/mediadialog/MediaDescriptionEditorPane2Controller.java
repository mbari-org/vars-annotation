package org.mbari.vars.ui.javafx.mediadialog;

import java.net.MalformedURLException;
import com.jfoenix.controls.JFXButton;
import org.mbari.vars.services.model.Media;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vars.ui.util.JFXUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.internal.operators.flowable.FlowableAllSingle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import mbarix4j.net.URLUtilities;

public class MediaDescriptionEditorPane2Controller {

  private static final Logger log = LoggerFactory.getLogger(MediaDescriptionEditorPane2Controller.class);

  private final ObjectProperty<Media> media = new SimpleObjectProperty<>();

  private final UIToolBox toolBox;

  private VBox root;
  private TitledPane videoSequenceTitledPane;
  private TextArea videoSequenceTextArea;
  private TitledPane videoTitledPane;
  private TextArea videoTextArea;
  private TitledPane videoReferenceTitledPane;
  private TextArea videoReferenceTextArea;
  private Button saveButton;



  public MediaDescriptionEditorPane2Controller(UIToolBox toolBox) {
    this.toolBox = toolBox;
    init();
  }

  private void init() {

    videoSequenceTextArea = new TextArea();
    videoSequenceTitledPane = new TitledPane();
    videoSequenceTitledPane.setText("Video Sequence");
    videoSequenceTitledPane.setCollapsible(false);
    videoSequenceTitledPane.setContent(videoSequenceTextArea);

    videoTextArea = new TextArea();
    videoTitledPane = new TitledPane();
    videoTitledPane.setText("Video Reference");
    videoTitledPane.setCollapsible(false);
    videoTitledPane.setContent(videoTextArea);

    videoReferenceTextArea = new TextArea();
    videoReferenceTitledPane = new TitledPane();
    videoReferenceTitledPane.setText("Video Reference");
    videoReferenceTitledPane.setCollapsible(false);
    videoReferenceTitledPane.setContent(videoReferenceTextArea);

    videoReferenceTextArea.textProperty().addListener(change -> checkDisable());
    videoTextArea.textProperty().addListener(change -> checkDisable());
    videoSequenceTextArea.textProperty().addListener(change -> checkDisable());

    var hbox = new HBox();
    var filler = new Region();
    saveButton = new JFXButton("Save");
    saveButton.setOnAction(e -> updateMedia());
    hbox.getChildren().addAll(filler, saveButton);
    HBox.setHgrow(filler, Priority.ALWAYS);

    media.addListener((obs, oldValue, newValue) -> {
      updateView(newValue);
    });

    root = new VBox(videoSequenceTitledPane, videoTitledPane, videoReferenceTitledPane, hbox);

    resetView();
  }

  public VBox getRoot() {
    return root;
  }

  private void checkDisable() {
    var disable = media.get() == null
        || (check(videoReferenceTextArea.getText(), media.get().getDescription())
            && check(videoTextArea.getText(), media.get().getVideoDescription())
            && check(videoSequenceTextArea.getText(), media.get().getVideoSequenceDescription()));
    
    log.debug("Disable: {}", disable);
    saveButton.setDisable(disable);
    if (!disable) {
      JFXUtilities.attractAttention(saveButton);
    }
    else {
      JFXUtilities.removeAttention(saveButton);
    }
  }

  private boolean check(String text, String description) {
    var same = false;
    if (text == null && description == null) {
      same = true;
    }
    else if (text != null && description != null) {
      same = text.equals(description);
    }
    return same;
  }

  public void setMedia(Media m) {
    log.debug("Setting media: {}", m);
    media.set(m);
  }

  private void updateView(Media m) {
    saveButton.setDisable(true);
    if (m == null) {
      resetView();
    } 
    else {
      videoSequenceTextArea.setText(m.getVideoSequenceDescription());
      videoSequenceTitledPane.setText(m.getVideoSequenceName());
      videoTextArea.setText(m.getVideoDescription());
      videoTitledPane.setText(m.getVideoName());
      videoReferenceTextArea.setText(m.getDescription());

      var uri = m.getUri();
      var scheme = uri.getScheme();
      String name = uri.toString();
      if (scheme.startsWith("http") || scheme.startsWith("file")) {
        try {
          name = URLUtilities.toFilename(uri.toURL());
        } catch (MalformedURLException e) {
          // TODO Auto-generated catch block
        }
        
      }
      videoReferenceTitledPane.setText(name);

      checkDisable();
    }
  }

  private void resetView() {
    videoSequenceTextArea.setText("");
    videoSequenceTitledPane.setText("Video Sequence");
    videoTextArea.setText("");
    videoTitledPane.setText("Video");
    videoReferenceTextArea.setText("");
    videoReferenceTitledPane.setText("Video Reference");
    saveButton.setDisable(true);
  }

  private void updateMedia() {
    var m = media.get();
    if (m != null) {
        m.setVideoSequenceDescription(videoSequenceTextArea.getText());
        m.setVideoDescription(videoTextArea.getText());
        m.setDescription(videoReferenceTextArea.getText());
        toolBox.getServices()
                .getMediaService()
                .update(m)
                .thenAccept(this::setMedia);
    }
}



}
