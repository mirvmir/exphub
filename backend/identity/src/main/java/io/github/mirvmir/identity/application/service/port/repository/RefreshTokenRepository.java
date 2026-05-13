package io.github.mirvmir.identity.application.service.port.repository;

import io.github.mirvmir.identity.domain.RefreshToken;

public interface RefreshTokenRepository {
    RefreshToken findByTokenHash(String tokenHash);
    RefreshToken save(RefreshToken refreshToken);
    void delete(RefreshToken refreshToken);
}
