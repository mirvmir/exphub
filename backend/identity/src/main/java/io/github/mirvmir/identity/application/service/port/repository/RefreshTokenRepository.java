package io.github.mirvmir.identity.application.service.port.repository;

import io.github.mirvmir.identity.domain.RefreshToken;

import java.time.Instant;

public interface RefreshTokenRepository {
    RefreshToken findByTokenHash(String tokenHash);
    RefreshToken save(RefreshToken refreshToken);
    void delete(RefreshToken refreshToken);
    int deleteExpiredBatch(Instant now, int batchSize);
}
