package org.mbari.m3.vars.annotation.ui;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.mbari.m3.vars.annotation.AppConfig;
import org.mbari.m3.vars.annotation.EventBus;
import org.mbari.m3.vars.annotation.UIToolBox;
import org.mbari.m3.vars.annotation.model.Annotation;
import org.mbari.m3.vars.annotation.model.AnnotationCount;
import org.mbari.m3.vars.annotation.model.Media;
import org.mbari.m3.vars.annotation.services.AnnotationService;
import org.mbari.m3.vars.annotation.services.RequestPager;
import org.mbari.m3.vars.annotation.services.annosaurus.v2.AnnoServiceV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * @author Brian Schlining
 * @since 2019-05-14T14:45:00
 */
public class AnnotationServiceDecorator2 {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final UIToolBox toolBox;
    private final int pageSize;
    private final Duration pageTimeout;
    private final int numberSimultaneousPages;

    private class RunUnit {
        private final Observable<Annotation> observable;
        private final Runnable starter;

        public RunUnit(Observable<Annotation> observable, Runnable starter) {
            this.observable = observable;
            this.starter = starter;
        }

        public Observable<Annotation> getObservable() {
            return observable;
        }

        public Runnable getStarter() {
            return starter;
        }
    }

    public AnnotationServiceDecorator2(UIToolBox toolBox) {
        this.toolBox = toolBox;
        AppConfig appConfig = toolBox.getAppConfig();
        pageSize = appConfig.getAnnotationServiceV1PageSize();
        pageTimeout = appConfig.getAnnotationServiceParamsV1().getTimeout();
        numberSimultaneousPages = appConfig.getAnnotationsServiceV1PageCount();
    }

    public RunUnit fetchAnnotations(AnnotationCount ac) {
//        AnnotationService service1 = toolBox.getServices().getAnnotationService();
        AnnoServiceV2 service2 = toolBox.getServices().getAnnoServiceV2();

        Function<RequestPager.Page, List<Annotation>> function = (page) -> {
            try {
                return service2.findAnnotations(ac.getVideoReferenceUuid(), page.getLimit(), page.getOffset())
                        .get(pageTimeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.info("A page request for " + ac.getVideoReferenceUuid() + " failed.", e);
                throw new RuntimeException(e);
            }
        };

        RequestPager<List<Annotation>> pager = new RequestPager<>(function, 2, numberSimultaneousPages);
        RequestPager.Runner<List<Annotation>> runner = pager.build(ac.getCount(), pageSize);
        Observable<List<Annotation>> observable = runner.getObservable();

        Subject<Annotation> s0 = new PublishSubject<>();
        Subject<Annotation> subject = s0.toSerialized();
        observable.subscribeOn(Schedulers.io())
                .subscribe(annotations -> annotations.forEach(subject::onNext),
                        subject::onError,
                        () -> log.debug("Loaded annotations for {}", ac.getVideoReferenceUuid()));


        return new RunUnit(subject, runner::run);

    }


}
