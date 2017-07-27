package org.mbari.m3.vars.annotation.events;

import org.mbari.m3.vars.annotation.model.User;

/**
 * @author Brian Schlining
 * @since 2017-07-27T11:57:00
 */
public class UserAddedEvent extends UIEvent<User> {

    public UserAddedEvent(User user) {
        super(null, user);
    }

}
