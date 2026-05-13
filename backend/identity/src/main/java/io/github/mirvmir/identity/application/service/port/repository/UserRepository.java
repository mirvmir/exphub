package io.github.mirvmir.identity.application.service.port.repository;

import io.github.mirvmir.identity.domain.User;

public interface UserRepository {
    User save(User user);
    boolean existUser(Long userId);
    User findById(Long userId);
    User findByEmail(String email);
}
