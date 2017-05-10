package org.mbari.m3.vars.annotation;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * @author Brian Schlining
 * @since 2017-05-10T09:55:00
 */
public class EventBus {

    private final Subject<Object, Object> rxSubject = new SerializedSubject<>(PublishSubject.create());

    public void send(Object o) {
        rxSubject.onNext(o);
    }

    public Observable<Object> toObserverable() {
        return rxSubject;
    }
}