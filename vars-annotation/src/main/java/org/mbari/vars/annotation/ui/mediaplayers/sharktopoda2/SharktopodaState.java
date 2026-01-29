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

    public boolean isDifferentThanSelected(Collection<UUID> localizations) {
//        var selected = new HashSet<>(selectedLocalizations);
//        var copy = new HashSet<>(localizations);
//        return !selected.equals(copy);
        var isDiff = true;
        if (!localizations.isEmpty() && !selectedLocalizations.isEmpty()) {
            var copy = new HashSet<>(localizations);
            var copySelected = new HashSet<>(selectedLocalizations);
            if (copy.size() == copySelected.size()) {
                copy.removeAll(copySelected);
                isDiff = !copy.isEmpty();
            }
        }
        return isDiff;
    }
}
