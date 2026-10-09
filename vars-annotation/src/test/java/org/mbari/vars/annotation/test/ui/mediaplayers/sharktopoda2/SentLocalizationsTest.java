package org.mbari.vars.annotation.test.ui.mediaplayers.sharktopoda2;

import org.junit.jupiter.api.Test;
import org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2.SentLocalizations;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SentLocalizationsTest {

    private final UUID obs = UUID.randomUUID();
    private final UUID otherObs = UUID.randomUUID();
    private final UUID box1 = UUID.randomUUID();
    private final UUID box2 = UUID.randomUUID();
    private final UUID otherBox = UUID.randomUUID();

    private static SentLocalizations.Changes changes(Set<UUID> add, Set<UUID> update, Set<UUID> remove) {
        return new SentLocalizations.Changes(add, update, remove);
    }

    /** e.g. the add localization button adds a box to an annotation that already has one */
    @Test
    public void newBoxOnChangedAnnotationIsAdded() {
        var sent = new SentLocalizations();
        sent.added(Map.of(box1, obs));
        var c = sent.changed(Set.of(obs), Map.of(box1, obs, box2, obs));
        assertEquals(changes(Set.of(box2), Set.of(box1), Set.of()), c);
    }

    @Test
    public void addedBoxIsUpdatedNextTime() {
        var sent = new SentLocalizations();
        sent.changed(Set.of(obs), Map.of(box1, obs));
        var c = sent.changed(Set.of(obs), Map.of(box1, obs));
        assertEquals(changes(Set.of(), Set.of(box1), Set.of()), c);
    }

    /** e.g. undoing the add localization button */
    @Test
    public void boxMissingFromChangedAnnotationIsRemoved() {
        var sent = new SentLocalizations();
        sent.added(Map.of(box1, obs, box2, obs));
        var c = sent.changed(Set.of(obs), Map.of(box1, obs));
        assertEquals(changes(Set.of(), Set.of(box1), Set.of(box2)), c);

        // Once removed, it's treated as new if it comes back (e.g. redo)
        c = sent.changed(Set.of(obs), Map.of(box1, obs, box2, obs));
        assertEquals(changes(Set.of(box2), Set.of(box1), Set.of()), c);
    }

    @Test
    public void lastBoxRemovedFromChangedAnnotationIsRemoved() {
        var sent = new SentLocalizations();
        sent.added(Map.of(box1, obs));
        var c = sent.changed(Set.of(obs), Map.of());
        assertEquals(changes(Set.of(), Set.of(), Set.of(box1)), c);
    }

    @Test
    public void boxesOnUnchangedAnnotationsAreLeftAlone() {
        var sent = new SentLocalizations();
        sent.added(Map.of(box1, obs, otherBox, otherObs));
        var c = sent.changed(Set.of(obs), Map.of(box1, obs));
        assertEquals(changes(Set.of(), Set.of(box1), Set.of()), c);
    }

    @Test
    public void removedBoxIsAddedIfItComesBack() {
        var sent = new SentLocalizations();
        sent.added(Map.of(box1, obs));
        sent.removed(Set.of(box1));
        var c = sent.changed(Set.of(obs), Map.of(box1, obs));
        assertEquals(changes(Set.of(box1), Set.of(), Set.of()), c);
    }
}
