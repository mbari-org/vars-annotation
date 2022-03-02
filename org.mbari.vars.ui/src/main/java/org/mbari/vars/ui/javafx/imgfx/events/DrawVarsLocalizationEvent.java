package org.mbari.vars.ui.javafx.imgfx.events;

import org.mbari.vars.ui.javafx.imgfx.domain.VarsLocalization;

import java.util.UUID;

public record DrawVarsLocalizationEvent(VarsLocalization varsLocalization) implements Event {
}
