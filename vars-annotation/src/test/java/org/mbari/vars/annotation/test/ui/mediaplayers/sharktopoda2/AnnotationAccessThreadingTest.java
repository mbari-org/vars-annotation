package org.mbari.vars.annotation.test.ui.mediaplayers.sharktopoda2;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;
import org.mbari.vars.annosaurus.sdk.r1.models.BoundingBox;
import org.mbari.vars.annotation.etc.rxjava.EventBus;
import org.mbari.vars.annotation.ui.Data;
import org.mbari.vars.annotation.ui.Initializer;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.events.AnnotationsAddedEvent;
import org.mbari.vars.annotation.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.annotation.ui.events.OpenDoneEvent;
import org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2.IncomingController;
import org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2.OutgoingController;
import org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2.SharktopodaState;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.remote.control.RemoteControl;
import org.mbari.vcr4j.remote.control.commands.localization.AddLocalizationsCmd;
import org.mbari.vcr4j.remote.control.commands.localization.SelectLocalizationsCmd;

import java.net.DatagramSocket;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Data.annotations is a plain JavaFX ObservableList owned by the FX application thread
 * (Data.setAnnotations mutates it via Platform.runLater). The sharktopoda2 controllers
 * react to events on other threads — PlayerIO's UDP receive thread, the toolbox executor —
 * and must not iterate the list there: a concurrent mutation throws
 * ConcurrentModificationException inside the Rx subscriber, which cancels the subscription
 * and silently kills localization handling until the media is reopened. All reads must be
 * snapshotted on the FX thread.
 */
public class AnnotationAccessThreadingTest {

    private UIToolBox toolBox;
    private final AtomicBoolean recording = new AtomicBoolean(false);
    private final CopyOnWriteArrayList<Boolean> readsOnFxThread = new CopyOnWriteArrayList<>();
    private Media media;
    private Annotation annotation;
    private Association box;
    private RemoteControl remoteControl;
    private IncomingController incomingController;
    private OutgoingController outgoingController;

    @BeforeAll
    public static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        }
        catch (IllegalStateException e) {
            // Toolkit already running
        }
    }

    @BeforeEach
    public void setup() throws Exception {
        var real = Initializer.getToolBox();
        var data = new Data() {
            @Override
            public ObservableList<Annotation> getAnnotations() {
                if (recording.get()) {
                    readsOnFxThread.add(Platform.isFxApplicationThread());
                }
                return super.getAnnotations();
            }
        };
        toolBox = new UIToolBox(data,
                real.getServices(),
                new EventBus(),
                real.getI18nBundle(),
                real.getConfig(),
                real.getStylesheets(),
                real.getExecutorService(),
                real.getAes());

        media = new Media();
        media.setVideoReferenceUuid(UUID.randomUUID());
        media.setUri(URI.create("http://localhost/test.mp4"));
        data.setMedia(media);

        box = new Association(BoundingBox.LINK_NAME,
                Association.VALUE_SELF,
                """
                        {"x": 10, "y": 20, "width": 30, "height": 40}""",
                "application/json",
                UUID.randomUUID());
        annotation = new Annotation("Nanomia bijuga", "brian",
                new VideoIndex(Duration.ofSeconds(1)),
                media.getVideoReferenceUuid());
        annotation.setObservationUuid(UUID.randomUUID());
        annotation.setAssociations(List.of(box));
        data.getAnnotations().setAll(List.of(annotation));

        int remotePort;
        int localPort;
        try (var a = new DatagramSocket(0); var b = new DatagramSocket(0)) {
            remotePort = a.getLocalPort();
            localPort = b.getLocalPort();
        }
        remoteControl = new RemoteControl.Builder(media.getVideoReferenceUuid())
                .remotePort(remotePort)
                .port(localPort)
                .build()
                .get();
        var sharktopodaState = new SharktopodaState();
        incomingController = new IncomingController(toolBox, remoteControl, sharktopodaState);
        outgoingController = new OutgoingController(toolBox, remoteControl.getVideoIO(), sharktopodaState);

        readsOnFxThread.clear();
        recording.set(true);
    }

    @AfterEach
    public void cleanup() {
        recording.set(false);
        incomingController.close();
        outgoingController.close();
        remoteControl.close();
    }

    @Test
    public void incomingSelectReadsAnnotationsOnTheFxThread() {
        var selected = new CopyOnWriteArrayList<AnnotationsSelectedEvent>();
        toolBox.getEventBus()
                .toObserverable()
                .ofType(AnnotationsSelectedEvent.class)
                .subscribe(selected::add);

        // This is what PlayerIO's UDP receive thread does — the test thread stands in for it
        remoteControl.getRequestHandler()
                .handleSelectLocalizationsRequest(new SelectLocalizationsCmd.Request(
                        media.getVideoReferenceUuid(), List.of(box.getUuid())));

        assertEquals(1, selected.size(), "The incoming selection was not handled");
        assertEquals(List.of(annotation), List.copyOf(selected.get(0).get()));
        assertFalse(readsOnFxThread.isEmpty(), "Expected the handler to read the annotations");
        assertTrue(readsOnFxThread.stream().allMatch(Boolean::booleanValue),
                "The annotation list was read off the FX thread: " + readsOnFxThread);
    }

    @Test
    public void localizationsAreSentOnlyAfterOpenDone() {
        var adds = new CopyOnWriteArrayList<AddLocalizationsCmd>();
        var disposable = remoteControl.getVideoIO()
                .getCommandSubject()
                .ofType(AddLocalizationsCmd.class)
                .subscribe(adds::add);
        try {
            toolBox.getEventBus().send(new AnnotationsAddedEvent(List.of(annotation)));
            assertTrue(adds.isEmpty(), "Localizations were sent before 'open done'");

            toolBox.getEventBus().send(new OpenDoneEvent(media.getVideoReferenceUuid()));
            assertEquals(1, adds.size(), "The full localization set was not sent on 'open done'");
        }
        finally {
            disposable.dispose();
        }
    }
}
