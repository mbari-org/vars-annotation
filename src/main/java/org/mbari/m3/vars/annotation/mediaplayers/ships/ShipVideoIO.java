package org.mbari.m3.vars.annotation.mediaplayers.ships;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.mbari.vcr4j.*;

/**
 * @author Brian Schlining
 * @since 2017-12-20T15:47:00
 */
public class ShipVideoIO implements VideoIO<ShipVideoState, SimpleVideoError> {

    public ShipVideoIO(String connectionID) {
        PublishSubject<VideoCommand> s1 = PublishSubject.create();
        Subject<VideoCommand> commandSubject = s1.toSerialized();

        PublishSubject<ShipVideoState> s2 = PublishSubject.create();
    }



    @Override
    public <A extends VideoCommand> void send(A videoCommand) {

    }

    @Override
    public Subject<VideoCommand> getCommandSubject() {
        return null;
    }

    @Override
    public String getConnectionID() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public Observable<SimpleVideoError> getErrorObservable() {
        return null;
    }

    @Override
    public Observable<ShipVideoState> getStateObservable() {
        return null;
    }

    @Override
    public Observable<VideoIndex> getIndexObservable() {
        return null;
    }
}
