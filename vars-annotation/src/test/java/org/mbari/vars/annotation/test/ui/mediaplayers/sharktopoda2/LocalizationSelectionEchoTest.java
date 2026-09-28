package org.mbari.vars.annotation.test.ui.mediaplayers.sharktopoda2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mbari.vars.annosaurus.sdk.r1.models.Annotation;
import org.mbari.vars.annosaurus.sdk.r1.models.Association;
import org.mbari.vars.annosaurus.sdk.r1.models.BoundingBox;
import org.mbari.vars.annotation.ui.Initializer;
import org.mbari.vars.annotation.ui.UIToolBox;
import org.mbari.vars.annotation.ui.events.AnnotationsSelectedEvent;
import org.mbari.vars.annotation.ui.events.OpenDoneEvent;
import org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2.IncomingController;
import org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2.OutgoingController;
import org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2.SharktopodaState;
import org.mbari.vars.vampiresquid.sdk.r1.models.Media;
import org.mbari.vcr4j.VideoIndex;
import org.mbari.vcr4j.remote.control.RemoteControl;
import org.mbari.vcr4j.remote.control.commands.localization.SelectLocalizationsCmd;

import java.net.DatagramSocket;
import java.net.URI;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Selection events must flow VARS → Sharktopoda, but a selection that Sharktopoda itself
 * initiated must not be echoed back to it — the echo can disagree with what the user
 * selected (an annotation with several bounding boxes expands to all of its localizations)
 * and the two apps end up re-selecting at each other.
 */
public class LocalizationSelectionEchoTest {

    private UIToolBox toolBox;
    private RemoteControl remoteControl;
    private IncomingController incomingController;
    private OutgoingController outgoingController;
    private Media media;
    private Annotation annotation;
    private Association box1;
    private Association box2;
    private final CopyOnWriteArrayList<SelectLocalizationsCmd> selectsSentToSharktopoda = new CopyOnWriteArrayList<>();

    private static Association boundingBoxAssociation() {
        return new Association(BoundingBox.LINK_NAME,
                Association.VALUE_SELF,
                """
                        {"x": 10, "y": 20, "width": 30, "height": 40}""",
                "application/json",
                UUID.randomUUID());
    }

    @BeforeEach
    public void setup() throws Exception {
        toolBox = Initializer.getToolBox();

        media = new Media();
        media.setVideoReferenceUuid(UUID.randomUUID());
        media.setUri(URI.create("http://localhost/test.mp4"));
        toolBox.getData().setMedia(media);

        // One annotation with TWO bounding boxes: selecting a single localization in
        // Sharktopoda selects the whole annotation in VARS, which expands to a different
        // set of localization UUIDs
        box1 = boundingBoxAssociation();
        box2 = boundingBoxAssociation();
        annotation = new Annotation("Nanomia bijuga", "brian",
                new VideoIndex(Duration.ofSeconds(1)),
                media.getVideoReferenceUuid());
        annotation.setObservationUuid(UUID.randomUUID());
        annotation.setAssociations(List.of(box1, box2));
        toolBox.getData().getAnnotations().setAll(List.of(annotation));

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
        outgoingController = new OutgoingController(toolBox, remoteControl.getVideoIO(), sharktopodaState);
        incomingController = new IncomingController(toolBox, remoteControl, sharktopodaState);

        // OutgoingController ignores everything until the player reports 'open done'
        toolBox.getEventBus().send(new OpenDoneEvent(media.getVideoReferenceUuid()));

        selectsSentToSharktopoda.clear();
        remoteControl.getVideoIO()
                .getCommandSubject()
                .ofType(SelectLocalizationsCmd.class)
                .subscribe(selectsSentToSharktopoda::add);
    }

    @AfterEach
    public void cleanup() {
        incomingController.close();
        outgoingController.close();
        remoteControl.close();
        toolBox.getData().getAnnotations().clear();
        toolBox.getData().setMedia(null);
    }

    @Test
    public void selectionFromSharktopodaIsNotEchoedBack() {
        // Sharktopoda tells VARS the user selected one localization. All handling is
        // synchronous, so any echo has been sent by the time this returns.
        remoteControl.getRequestHandler()
                .handleSelectLocalizationsRequest(new SelectLocalizationsCmd.Request(
                        media.getVideoReferenceUuid(), List.of(box1.getUuid())));

        assertEquals(List.of(), selectsSentToSharktopoda,
                "A selection initiated by Sharktopoda was echoed back to it");
    }

    @Test
    public void selectionFromVarsIsSentToSharktopoda() {
        toolBox.getEventBus().send(new AnnotationsSelectedEvent(new Object(), List.of(annotation)));

        assertEquals(1, selectsSentToSharktopoda.size(),
                "A selection made in VARS was not sent to Sharktopoda");
        var selectedUuids = new HashSet<>(selectsSentToSharktopoda.get(0).getValue().getLocalizations());
        assertEquals(Set.of(box1.getUuid(), box2.getUuid()), selectedUuids);
    }
}
