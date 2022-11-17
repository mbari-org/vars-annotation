package org.mbari.vars.ui.javafx.annotable;

import javafx.scene.control.TableCell;
import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.services.model.Association;

import java.util.Comparator;
import java.util.List;

public class FixedHeightAssociationCell extends TableCell<Annotation, List<Association>> {



    private static final Comparator<Association> alphabetical = Comparator.comparing(Association::toString);

}
