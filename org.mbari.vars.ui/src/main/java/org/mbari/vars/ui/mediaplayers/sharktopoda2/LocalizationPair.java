package org.mbari.vars.ui.mediaplayers.sharktopoda2;

import org.mbari.vars.services.model.Annotation;
import org.mbari.vars.ui.UIToolBox;
import org.mbari.vcr4j.remote.control.commands.localization.Localization;

import java.util.List;

/**
 * When the remote video player sends a command to update, select, or delete a localization to VARS,
 * the localization's UUID corresponds to an existing Association. We then have to match the
 * localization to the assocation and it's parent annotations. This container holds all three of those.
 * @param localizedAnnotation The Annotation and association corresponding to a localization
 * @param localization The localization send from the video app to VARS
 */
public record LocalizationPair(LocalizedAnnotation localizedAnnotation,
                               Localization localization) implements Comparable<LocalizationPair> {

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LocalizationPair that) {
            return this.localization.getUuid().equals(that.localization.getUuid());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return localization.getUuid().hashCode();
    }

    @Override
    public int compareTo(LocalizationPair that) {
        return this.localization.getUuid().compareTo(that.localization.getUuid());
    }

}
