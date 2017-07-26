package org.mbari.m3.vars.annotation.commands;

/**
 * @author Brian Schlining
 * @since 2017-06-28T13:08:00
 */
public class ChangeUserMsg implements Message {

    private final String username;

    public ChangeUserMsg(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
