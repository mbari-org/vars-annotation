package demos;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class SliderListenerDemo extends Application {

    @Override
    public void start(Stage stage) {

        Label label = new Label("Select Brightness:");

        Label infoLabel = new Label("-");
        infoLabel.setTextFill(Color.BLUE);

        Label changingLabel = new Label("-");
        changingLabel.setTextFill(Color.ORANGERED);

        Slider slider = new Slider();

        slider.setMin(0);
        slider.setMax(100);
        slider.setValue(80);

        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);

        slider.setBlockIncrement(10);

        // Adding Listener to value property.
        //
        slider.valueProperty().addListener((observable, oldValue, newValue) -> infoLabel.setText("New value: " + newValue));

        slider.valueChangingProperty().addListener((obs, oldv, newv) -> changingLabel.setText("" + newv));

        VBox root = new VBox();
        root.setPadding(new Insets(20));
        root.setSpacing(10);
        root.getChildren().addAll(label, slider, infoLabel, changingLabel);

        stage.setTitle("JavaFX Slider (o7planning.org)");
        Scene scene = new Scene(root, 350, 200);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

}

