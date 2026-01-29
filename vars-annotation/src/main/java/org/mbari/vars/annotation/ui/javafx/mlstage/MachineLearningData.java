package org.mbari.vars.annotation.ui.javafx.mlstage;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.image.ImageView;
import org.mbari.imgfx.roi.Localization;
import org.mbari.imgfx.roi.RectangleView;

import java.util.List;

public class MachineLearningData {

    ObservableList<Localization<RectangleView, ImageView>> localizations = FXCollections.observableArrayList();


}
