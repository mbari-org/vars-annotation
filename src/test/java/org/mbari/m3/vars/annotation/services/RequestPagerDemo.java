package org.mbari.m3.vars.annotation.services;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.Subject;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Brian Schlining
 * @since 2019-04-24T16:13:00
 */
public class RequestPagerDemo {

    public static void main(String[] args) throws Exception {

        // 6e4d36c8-a0c0-4b9f-a012-4d2e7b515e43	D0859-03HD	urn:tid:mbari.org:D0859-03HD

        System.getProperties().setProperty("user.timezone", "UTC");
        UUID videoReferenceUuid = UUID.fromString("030ef551-c52e-44db-a857-e67edc61bf32");

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
                        .get(300, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("WTF?", e);
                throw new RuntimeException(e);
            }
        };

        AnnotationCount count = service.countAnnotations(videoReferenceUuid).get(10, TimeUnit.SECONDS);
        System.out.println("Found " + count.getCount() + " annotations");
        RequestPager<List<Annotation>> pager = new RequestPager<>(function, 3, 2);
        RequestPager.Runner<List<Annotation>> runner = pager.build(count.getCount(), 30);
        Observable<List<Annotation>> observable = runner.getObservable();
        observable.subscribeOn(Schedulers.io())
                .subscribe(annotations::addAll,
                    e -> log.error("Bummer!", e),
                    () -> {
                        log.info("Completed!");
                        if (count.getCount() != annotations.size()) {
                            log.error("We did not fetch all the annotations. Expected " + count.getCount() + ". Found " + annotations.size());
                        }
                        System.exit(0);
                    });
        runner.run();
    }
}
