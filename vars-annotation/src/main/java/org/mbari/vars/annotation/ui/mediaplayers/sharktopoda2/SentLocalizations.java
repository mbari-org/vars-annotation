package org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2;

import java.util.*;

/**
 * Tracks the localizations that have been sent to Sharktopoda, so that a change to an annotation can
 * be sent as the right command. Sharktopoda ignores an "update localizations" for a localization it
 * doesn't have, so a bounding box added to an existing annotation must be sent as an "add". Likewise
 * a bounding box removed from an annotation that still exists must be sent as a "remove".
 */
public class SentLocalizations {

    /**
     * What to send to Sharktopoda for a change.
     * @param toAdd Localization UUIDs that Sharktopoda doesn't have yet
     * @param toUpdate Localization UUIDs that Sharktopoda already has
     * @param toRemove Localization UUIDs that Sharktopoda has but that no longer exist
     */
    public record Changes(Set<UUID> toAdd, Set<UUID> toUpdate, Set<UUID> toRemove) {}

    /** Localization UUID -> the observation UUID of the annotation it belongs to */
    private final Map<UUID, UUID> sent = new HashMap<>();

    /**
     * @param localizations Localization UUID -> observation UUID of the localizations that were sent
     */
    public synchronized void added(Map<UUID, UUID> localizations) {
        sent.putAll(localizations);
    }

    /**
     * @param localizationUuids UUIDs of the localizations that were removed
     */
    public synchronized void removed(Collection<UUID> localizationUuids) {
        localizationUuids.forEach(sent::remove);
    }

    /**
     * Works out what to send for changed annotations and records the result as sent.
     *
     * @param observationUuids The observation UUIDs of the changed annotations
     * @param current Localization UUID -> observation UUID of the localizations the changed annotations
     *                have now
     * @return What to send to Sharktopoda
     */
    public synchronized Changes changed(Set<UUID> observationUuids, Map<UUID, UUID> current) {
        var toRemove = new HashSet<UUID>();
        sent.forEach((localizationUuid, observationUuid) -> {
            if (observationUuids.contains(observationUuid) && !current.containsKey(localizationUuid)) {
                toRemove.add(localizationUuid);
            }
        });
        var toAdd = new HashSet<UUID>();
        var toUpdate = new HashSet<UUID>();
        current.keySet().forEach(uuid -> (sent.containsKey(uuid) ? toUpdate : toAdd).add(uuid));

        toRemove.forEach(sent::remove);
        sent.putAll(current);
        return new Changes(toAdd, toUpdate, toRemove);
    }
}
