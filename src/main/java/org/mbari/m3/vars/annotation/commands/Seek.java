package org.mbari.m3.vars.annotation.commands;

/**
 * @author Brian Schlining
 * @since 2017-06-02T11:49:00
 */
public class Seek<T> {

    private final T index;

    public Seek(T index) {
        this.index = index;
    }

    public T getIndex() {
        return index;
    }
}
