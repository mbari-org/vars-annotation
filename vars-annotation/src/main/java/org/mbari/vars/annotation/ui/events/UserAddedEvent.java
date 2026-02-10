package org.mbari.vars.annotation.ui.events;

import org.mbari.vars.oni.sdk.r1.models.User;

/**
 * @author Brian Schlining
 * @since 2017-07-27T11:57:00
 */
public class UserAddedEvent extends UIEvent<User> {

    public UserAddedEvent(User user) {
        super(null, user);
    }

}
