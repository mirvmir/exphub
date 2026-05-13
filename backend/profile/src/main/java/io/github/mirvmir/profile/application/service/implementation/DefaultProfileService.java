package io.github.mirvmir.profile.application.service.implementation;

import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.identity.dto.TokenDto;
import io.github.mirvmir.identity.dto.UserInfoDto;
import io.github.mirvmir.profile.api.event.ProfileCompletedEvent;
import io.github.mirvmir.profile.application.service.port.repository.ProfileRepository;
import io.github.mirvmir.profile.application.service.dto.EditProfileRqDto;
import io.github.mirvmir.profile.application.service.interfaces.ProfileService;
import io.github.mirvmir.profile.domain.Profile;
import io.github.mirvmir.profile.application.service.dto.EditProfileRsDto;
import io.github.mirvmir.profile.web.response.MyProfileResponse;
import io.github.mirvmir.profile.web.response.PublicProfileResponse;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class DefaultProfileService implements ProfileService {

    private final IdentityApi identityApi;

    private final ProfileRepository profileRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public EditProfileRsDto editProfile(EditProfileRqDto dto) {
        Long currentUserId = identityApi.getCurrentUserId();
        Profile profile = profileRepository.findByUserId(currentUserId);

        boolean wasCompleted  = profile.isCompleted();

        profile.change(
                dto.givenName(),
                dto.familyName(),
                dto.fatherName(),
                dto.avatarFileId(),
                dto.emailVisibility()
        );

        profileRepository.save(profile);

        if (!wasCompleted && profile.isCompleted()) {
            eventPublisher.publishEvent(
                    new ProfileCompletedEvent(currentUserId)
            );

            TokenDto tokenDto = identityApi.reissueTokens(currentUserId);

            return new EditProfileRsDto(
                    true,
                    tokenDto.accessToken(),
                    tokenDto.refreshToken()
            );
        }

        return new EditProfileRsDto(
                profile.isCompleted(),
                null,
                null
        );
    }

    @Override
    public MyProfileResponse getMyProfile() {
        UserInfoDto currentUserInfo = identityApi.getCurrentUserInfo();
        Profile profile = profileRepository.findByUserId(currentUserInfo.userId());

        return new MyProfileResponse(
                profile.getId(),
                currentUserInfo.userId(),
                currentUserInfo.email(),
                profile.getGivenName(),
                profile.getFamilyName(),
                profile.getFatherName(),
                profile.getAvatarFileId(),
                profile.isCompleted()
        );
    }

    @Override
    public PublicProfileResponse getPublicProfile(Long id) {
        Profile profile = profileRepository.findByUserId(id);
        UserInfoDto user = identityApi.getUserInfoById(id);

        String email = profile.isEmailVisibility()
                ? user.email()
                : null;

        return new PublicProfileResponse(
                profile.getId(),
                user.userId(),
                email,
                profile.getGivenName(),
                profile.getFamilyName(),
                profile.getFatherName(),
                profile.getAvatarFileId()
        );
    }
}