package org.mbari.m3.vars.annotation.events;

import org.mbari.m3.vars.annotation.model.User;

/**
 * @author Brian Schlining
 * @since 2017-07-20T17:23:00
 */
public class UserChangedEvent extends UIChangeEvent<User> {

    public UserChangedEvent(Object changeSource, User user) {
        super(changeSource, user);
    }

    public UserChangedEvent(User user) {
        this(null, user);
    }
}
