package org.mbari.m3.vars.annotation.mediaplayers.ships;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.mbari.m3.vars.annotation.services.ImageCaptureService;
import org.mbari.vcr4j.*;
import org.mbari.vcr4j.commands.VideoCommands;

import java.time.Instant;

/**
 * @author Brian Schlining
 * @since 2017-12-20T15:47:00
 */
public class ShipVideoIO implements VideoIO<ShipVideoState, SimpleVideoError> {

    private final String connectionId;

    private final Subject<VideoCommand> commandSubject;

    private final Subject<SimpleVideoError> errorSubject;

    private final Subject<VideoIndex> indexSubject;

    private final Subject<ShipVideoState> stateSubject;

    public ShipVideoIO(String connectionId) {

        this.connectionId = connectionId;

        PublishSubject<VideoCommand> s1 = PublishSubject.create();
        commandSubject = s1.toSerialized();

        PublishSubject<ShipVideoState> s2 = PublishSubject.create();
        stateSubject = s2.toSerialized();

        PublishSubject<SimpleVideoError> s3 = PublishSubject.create();
        errorSubject = s3.toSerialized();

        PublishSubject<VideoIndex> s4 = PublishSubject.create();
        indexSubject = s4.toSerialized();

        commandSubject.filter(vc -> vc.equals(VideoCommands.REQUEST_INDEX) ||
                vc.equals(VideoCommands.REQUEST_TIMESTAMP))
                .subscribe(vc -> requestIndex());

    }

    private void requestIndex() {
        Instant now = Instant.now();
        VideoIndex vi = new VideoIndex(now);
        indexSubject.onNext(vi);
    }

    @Override
    public <A extends VideoCommand> void send(A videoCommand) {
        commandSubject.onNext(videoCommand);
    }

    @Override
    public Subject<VideoCommand> getCommandSubject() {
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
    public Observable<ShipVideoState> getStateObservable() {
        return stateSubject;
    }

    @Override
    public Observable<VideoIndex> getIndexObservable() {
        return indexSubject;
    }
}
