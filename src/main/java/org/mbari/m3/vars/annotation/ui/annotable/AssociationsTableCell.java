package org.mbari.m3.vars.annotation.ui.annotable;

import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import org.mbari.m3.vars.annotation.model.Association;

/**
 * @author Brian Schlining
 * @since 2017-05-22T16:20:00
 */
public class AssociationsTableCell<S> extends TableCell<S, Association> {

    private ListView<Association> listView;

    public AssociationsTableCell() {
        getStyleClass().add("annotable-tablecell");
    }
}
