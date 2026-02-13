package org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import org.mbari.vcr4j.VideoCommand;
import org.mbari.vcr4j.VideoIO;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.remote.control.RError;
import org.mbari.vcr4j.remote.control.RState;

public class NoopVideoIO implements VideoIO<RState, RError> {

    private final String connectionId;

    private final Subject<VideoCommand<?>> commandSubject;

    private final Subject<RError> errorSubject;

    private final Subject<VideoIndex> indexSubject;

    private final Subject<RState> stateSubject;

    public NoopVideoIO(String connectionId) {
        this.connectionId = connectionId;

        PublishSubject<VideoCommand<?>> s1 = PublishSubject.create();
        commandSubject = s1.toSerialized();

        PublishSubject<RState> s2 = PublishSubject.create();
        stateSubject = s2.toSerialized();

        PublishSubject<RError> s3 = PublishSubject.create();
        errorSubject = s3.toSerialized();

        PublishSubject<VideoIndex> s4 = PublishSubject.create();
        indexSubject = s4.toSerialized();

        commandSubject.ofType(VideoCommand.class)
                .subscribe(vc -> {
                    var error = new RError(true, false, false, vc);
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
    public Observable<RError> getErrorObservable() {
        return errorSubject;
    }

    @Override
    public Observable<RState> getStateObservable() {
        return stateSubject;
    }

    @Override
    public Observable<VideoIndex> getIndexObservable() {
        return indexSubject;
    }
}
