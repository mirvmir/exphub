package io.github.mirvmir.identity.application.service.interfaces;

import io.github.mirvmir.identity.application.service.dto.RefreshDto;

public interface RefreshService {
    RefreshDto execute(String rawToken);
    void deleteExpiredRefreshTokens();
}
