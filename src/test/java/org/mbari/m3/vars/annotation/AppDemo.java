package org.mbari.m3.vars.annotation;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test for simple App.
 */
public class AppDemo extends App {

    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger(AppDemo.class);
        Initializer.getToolBox()
                .getEventBus()
                .toObserverable()
                .subscribe(e -> log.debug(e.toString()));
        App.main(args);

        ObservableList<Annotation> annotations = Initializer.getToolBox()
                .getData()
                .getAnnotations();

        annotations.addListener(new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        System.out.println("Annotation count: " + annotations.size());
                    }
                });
    }
}
