package org.mbari.m3.vars.annotation.commands;

import org.mbari.m3.vars.annotation.UIToolBox;

/**
 * Encapsulates some bit of executable code. Apply is used to execute, unapply should undo
 * everything. These commands are pushed onto the {@link org.mbari.m3.vars.annotation.EventBus}
 * and picked up and executed by the {@link CommandManager}.
 *
 * @author Brian Schlining
 * @since 2017-05-10T10:07:00
 */
public interface Command {

    void apply(UIToolBox toolBox);

    void unapply(UIToolBox toolBox);

    String getDescription();

}
