package org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReentrantLock;

public class SharktopodaState {

    private final Set<UUID> selectedLocalizations = new CopyOnWriteArraySet<>();
    private final ReentrantLock lock = new ReentrantLock();

    public void setSelectedLocalizations(Collection<UUID> selectedLocalizations) {
            lock.lock();
            this.selectedLocalizations.clear();
            if (selectedLocalizations != null) {
                this.selectedLocalizations.addAll(selectedLocalizations);
            }
            lock.unlock();
    }

    /**
     * Should a selection made in VARS be sent to Sharktopoda? Not if everything selected in
     * Sharktopoda is already part of it. e.g. the user selected one box of an annotation in Sharktopoda,
     * and VARS then (re)selects that annotation, which expands to all of its boxes. Sending that would
     * pull the selection away from the box the user is working on.
     *
     * @param localizations The localization UUIDs that VARS has selected
     * @return true if the selection should be sent to Sharktopoda
     */
    public boolean shouldSendSelection(Collection<UUID> localizations) {
        var selected = new HashSet<>(selectedLocalizations);
        boolean alreadyCovered = !selected.isEmpty() && localizations.containsAll(selected);
        return !alreadyCovered;
    }
}
