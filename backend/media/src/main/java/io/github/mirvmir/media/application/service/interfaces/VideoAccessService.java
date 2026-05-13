package io.github.mirvmir.media.application.service.interfaces;

public interface VideoAccessService {
    void checkCurrentUserCanWatch(Long videoId);
}
