package org.mbari.vars.ui.mediaplayers.sharktopoda2;

import org.mbari.vcr4j.sharktopoda.client.localization.Localization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class SharktopodaState {

    private final List<UUID> selectedLocalizations = new CopyOnWriteArrayList<>();

    public void setSelectedLocalizations(Collection<UUID> selectedLocalizations) {
        synchronized (this.selectedLocalizations) {
            this.selectedLocalizations.clear();
            this.selectedLocalizations.addAll(selectedLocalizations);
        }
    }

    public boolean isDifferentThanSelected(Collection<UUID> localizations) {
        var isDiff = true;
        if (!localizations.isEmpty() && !selectedLocalizations.isEmpty()) {
            var copy = new ArrayList<>(localizations);
            var copySelected = new ArrayList<>(selectedLocalizations);
            if (copy.size() == copySelected.size()) {
                copy.removeAll(copySelected);
                isDiff = copy.size() != 0;
            }
        }
        return isDiff;
    }
}
