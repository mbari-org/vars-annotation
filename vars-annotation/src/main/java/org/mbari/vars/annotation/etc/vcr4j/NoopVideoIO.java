package org.mbari.vars.annotation.etc.vcr4j;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.mbari.vcr4j.SimpleVideoError;
import org.mbari.vcr4j.VideoCommand;
import org.mbari.vcr4j.VideoIO;
import org.mbari.vcr4j.VideoIndex;

public class NoopVideoIO implements VideoIO<NoopVideoState, SimpleVideoError> {

    private final String connectionId;

    private final Subject<VideoCommand<?>> commandSubject;

    private final Subject<SimpleVideoError> errorSubject;

    private final Subject<VideoIndex> indexSubject;

    private final Subject<NoopVideoState> stateSubject;

    public NoopVideoIO(String connectionId) {
        this.connectionId = connectionId;

        PublishSubject<VideoCommand<?>> s1 = PublishSubject.create();
        commandSubject = s1.toSerialized();

        PublishSubject<NoopVideoState> s2 = PublishSubject.create();
        stateSubject = s2.toSerialized();

        PublishSubject<SimpleVideoError> s3 = PublishSubject.create();
        errorSubject = s3.toSerialized();

        PublishSubject<VideoIndex> s4 = PublishSubject.create();
        indexSubject = s4.toSerialized();

        commandSubject.ofType(VideoCommand.class)
                .subscribe(vc -> {
                    var error = new SimpleVideoError(true, vc);
                    errorSubject.onNext(error);
                });
    }

    @Override
    public <A extends VideoCommand<?>> void send(A a) {
        commandSubject.onNext(a);
    }

    @Override
    public Subject<VideoCommand<?>> getCommandSubject() {
        return commandSubject;
    }

    @Override
    public String getConnectionID() {
        return connectionId;
    }

    @Override
    public void close() {
        commandSubject.onComplete();
        indexSubject.onComplete();
        errorSubject.onComplete();
        stateSubject.onComplete();
    }

    @Override
    public Observable<SimpleVideoError> getErrorObservable() {
        return errorSubject;
    }

    @Override
    public Observable<NoopVideoState> getStateObservable() {
        return stateSubject;
    }

    @Override
    public Observable<VideoIndex> getIndexObservable() {
        return indexSubject;
    }
}
