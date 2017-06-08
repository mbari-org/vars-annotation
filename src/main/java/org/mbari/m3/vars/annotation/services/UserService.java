package org.mbari.m3.vars.annotation.services;

import org.mbari.m3.vars.annotation.model.User;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Brian Schlining
 * @since 2017-06-08T16:20:00
 */
public interface UserService {

    CompletableFuture<List<User>> findAllUsers();

    CompletableFuture<User> create(User user);

    CompletableFuture<Optional<User>> update(User user);


}
