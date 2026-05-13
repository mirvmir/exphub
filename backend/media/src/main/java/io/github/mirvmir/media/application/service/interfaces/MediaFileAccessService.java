package io.github.mirvmir.media.application.service.interfaces;

public interface MediaFileAccessService {
    void checkCurrentUserCanAccess(Long fileId);
    void checkCanBePublic(Long fileId);
}
