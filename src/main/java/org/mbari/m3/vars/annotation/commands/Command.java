package org.mbari.m3.vars.annotation.commands;

/**
 * @author Brian Schlining
 * @since 2017-05-10T10:07:00
 */
public interface Command<A> {

    void apply();

    void unapply();

    A get();

}
