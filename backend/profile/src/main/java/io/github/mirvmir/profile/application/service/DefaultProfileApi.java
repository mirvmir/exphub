package io.github.mirvmir.profile.application.service;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.profile.api.ProfileApi;
import io.github.mirvmir.profile.api.dto.ProfileNameDto;
import io.github.mirvmir.profile.api.dto.CreateProfileRequest;
import io.github.mirvmir.profile.application.service.port.repository.ProfileRepository;
import io.github.mirvmir.profile.application.service.mapper.ProfileNameMapper;
import io.github.mirvmir.profile.domain.Profile;
import io.github.mirvmir.profile.exception.ProfileErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class DefaultProfileApi implements ProfileApi {

    private final ProfileRepository profileRepository;
    private final ProfileNameMapper profileNameMapper;

    @Override
    @Transactional(readOnly = true)
    public ProfileNameDto getProfileName(Long userId) {
        Profile profile = profileRepository.findByUserId(userId);

        if (profile == null) {
            throw new NotFoundException(
                    ProfileErrorCode.PROFILE_NOT_FOUND,
                    "Profile with id=" + userId + " not found"
            );
        }

        return profileNameMapper.toDto(profile);
    }

    @Override
    @Transactional
    public Long createProfile(CreateProfileRequest request) {
        Profile profile = Profile.createNew(
                request.userId()
        );

        return profileRepository.save(profile).getId();
    }

    @Override
    public boolean existsPublicAvatar(Long fileId) {
        return profileRepository.existsAvatarByFileId(fileId);
    }
}
