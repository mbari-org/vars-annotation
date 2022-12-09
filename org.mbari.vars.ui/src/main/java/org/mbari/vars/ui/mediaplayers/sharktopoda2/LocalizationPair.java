package org.mbari.vars.ui.mediaplayers.sharktopoda2;

import org.mbari.vcr4j.remote.control.commands.localization.Localization;

public record LocalizationPair(LocalizedAnnotation localizedAnnotation, Localization localization) {
}
