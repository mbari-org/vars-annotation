package org.mbari.vars.ui.demos.javafx.concepttree;

import javafx.scene.control.TreeItem;

import java.util.function.Predicate;

/**
 * @author Brian Schlining
 * @since 2017-05-16T11:58:00
 */
@FunctionalInterface
public interface TreeItemPredicate<T> {

    boolean test(TreeItem<T> parent, T value);

    static <T> TreeItemPredicate<T> create(Predicate<T> predicate) {
        return (parent, value) -> predicate.test(value);
    }

}