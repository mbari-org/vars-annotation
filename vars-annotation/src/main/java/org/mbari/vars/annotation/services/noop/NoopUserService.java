package org.mbari.vars.annotation.services.noop;

import org.mbari.vars.oni.sdk.r1.UserService;
import org.mbari.vars.oni.sdk.r1.models.User;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class NoopUserService implements UserService {
    @Override
    public CompletableFuture<List<User>> findAllUsers() {
        var name = System.getProperty("user.name");
        var email = name + "@localhost";
        var user = new User(name, "", "", "", "", "", email);
        return CompletableFuture.completedFuture(List.of(user));
    }

    @Override
    public CompletableFuture<User> create(User user) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }

    @Override
    public CompletableFuture<Optional<User>> update(User user) {
        return CompletableFuture.failedFuture(new UnsupportedOperationException());
    }
}
