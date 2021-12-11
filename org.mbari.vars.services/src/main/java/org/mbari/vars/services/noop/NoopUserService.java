package org.mbari.vars.services.noop;

import org.mbari.vars.services.UserService;
import org.mbari.vars.services.model.User;

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
