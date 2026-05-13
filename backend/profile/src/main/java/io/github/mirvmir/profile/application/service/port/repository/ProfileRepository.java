package io.github.mirvmir.profile.application.service.port.repository;


import io.github.mirvmir.profile.domain.Profile;

public interface ProfileRepository {
    Profile save(Profile profile);
    Profile findByUserId(Long userId);
    boolean existsAvatarByFileId(Long fileId);
}
