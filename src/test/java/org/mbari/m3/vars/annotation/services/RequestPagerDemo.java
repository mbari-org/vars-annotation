package org.mbari.m3.vars.annotation.services;

import io.reactivex.Observable;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import org.mbari.m3.vars.annotation.AppDemo;
import org.mbari.m3.vars.annotation.Initializer;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.AnnotationCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * @author Brian Schlining
 * @since 2019-04-24T16:13:00
 */
public class RequestPagerDemo {

    public static void main(String[] args) throws Exception {

        System.getProperties().setProperty("user.timezone", "UTC");
        UUID videoReferenceUuid = UUID.fromString(args[0]);

        Logger log = LoggerFactory.getLogger(AppDemo.class);
        Initializer.getToolBox()
                .getEventBus()
                .toObserverable()
                .subscribe(e -> log.debug(e.toString()));


        ObservableList<Annotation> annotations = Initializer.getToolBox()
                .getData()
                .getAnnotations();

        annotations.addListener((InvalidationListener) observable ->
                log.debug("Annotation count: " + annotations.size()));

        AnnotationService service = Initializer.getToolBox()
                .getServices()
                .getAnnotationService();

        Function<RequestPager.Page, List<Annotation>> function = (page) -> {
            try {
                return service.findAnnotations(videoReferenceUuid, page.getLimit(), page.getOffset())
                        .get(30, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        AnnotationCount count = service.countAnnotations(videoReferenceUuid).get(10, TimeUnit.SECONDS);

        RequestPager<List<Annotation>> pager = new RequestPager<>(function, 3, 3);
        Observable<List<Annotation>> observable = pager.apply(count.getCount(), 50);


        observable.subscribe(annotations::addAll,
                e -> log.error("Bummer!", e),
                () -> log.info("Completed!"));
    }
}
