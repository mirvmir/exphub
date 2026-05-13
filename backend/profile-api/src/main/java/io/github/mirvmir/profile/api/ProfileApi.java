package io.github.mirvmir.profile.api;

import io.github.mirvmir.profile.api.dto.ProfileNameDto;
import io.github.mirvmir.profile.api.dto.CreateProfileRequest;

public interface ProfileApi {
    ProfileNameDto getProfileName(Long userId);
    Long createProfile(CreateProfileRequest request);
    boolean existsPublicAvatar(Long fileId);
}
