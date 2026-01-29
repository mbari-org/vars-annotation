package org.mbari.vars.annotation.ui.events;

import org.mbari.vars.oni.sdk.r1.models.User;

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
