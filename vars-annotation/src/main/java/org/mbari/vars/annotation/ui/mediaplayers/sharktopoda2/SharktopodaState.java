package org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;

public class SharktopodaState {

    /**
     * How long after VARS tells Sharktopoda to seek that an empty selection from Sharktopoda
     * is treated as a side effect of the seek rather than the user deselecting.
     */
    static final Duration SEEK_GRACE_PERIOD = Duration.ofSeconds(1);

    private final Set<UUID> selectedLocalizations = new CopyOnWriteArraySet<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final LongSupplier nanoClock;
    private volatile boolean hasSeeked = false;
    private volatile long lastSeekNanos;

    public SharktopodaState() {
        this(System::nanoTime);
    }

    /** @param nanoClock Source of {@link System#nanoTime()}-style timestamps. Replaceable for testing */
    SharktopodaState(LongSupplier nanoClock) {
        this.nanoClock = nanoClock;
    }

    /**
     * Call just <b>before</b> a seek command is sent to Sharktopoda. Sharktopoda clears its
     * localization selection when it seeks and reports that back as an empty selection; marking
     * the seek first guarantees the mark is in place before that report can arrive.
     */
    public void seekRequested() {
        lastSeekNanos = nanoClock.getAsLong();
        hasSeeked = true;
    }

    /**
     * @return true if VARS asked Sharktopoda to seek within the last {@link #SEEK_GRACE_PERIOD}
     */
    public boolean isWithinSeekGracePeriod() {
        if (!hasSeeked) {
            return false;
        }
        long elapsed = nanoClock.getAsLong() - lastSeekNanos;
        return elapsed >= 0 && elapsed <= SEEK_GRACE_PERIOD.toNanos();
    }

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
