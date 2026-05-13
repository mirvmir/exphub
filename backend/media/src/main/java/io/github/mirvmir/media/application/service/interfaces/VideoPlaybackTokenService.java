package io.github.mirvmir.media.application.service.interfaces;

public interface VideoPlaybackTokenService {
    String createToken(Long videoId);
    void validateToken(Long videoId, String token);
}