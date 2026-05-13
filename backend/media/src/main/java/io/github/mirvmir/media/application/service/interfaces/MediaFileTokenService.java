package io.github.mirvmir.media.application.service.interfaces;

public interface MediaFileTokenService {
    String createToken(Long fileId);
    void validateToken(Long fileId, String token);
}