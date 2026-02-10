package org.mbari.vars.annotation.ui.mediaplayers.sharktopoda2;

import org.mbari.vcr4j.remote.control.commands.localization.Localization;

/**
 * When the remote video player sends a command to update, select, or delete a localization to VARS,
 * the localization's UUID corresponds to an existing Association. We then have to match the
 * localization to the assocation and it's parent annotations. This container holds all three of those.
 * @param localizedAnnotation The Annotation and association corresponding to a localization
 * @param localization The localization send from the video app to VARS
 */
public record LocalizationPair(LocalizedAnnotation localizedAnnotation, Localization localization) {
}
