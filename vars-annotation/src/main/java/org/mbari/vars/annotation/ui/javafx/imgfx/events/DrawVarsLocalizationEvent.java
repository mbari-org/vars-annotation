package org.mbari.vars.annotation.ui.javafx.imgfx.events;

import org.mbari.vars.annotation.ui.javafx.imgfx.domain.VarsLocalization;


public record DrawVarsLocalizationEvent(VarsLocalization varsLocalization) implements Event {
}
