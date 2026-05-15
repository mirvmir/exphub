package io.github.mirvmir.profile.application.service.implementation;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.common.exception.UnauthorizedException;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.identity.dto.TokenDto;
import io.github.mirvmir.identity.dto.UserInfoDto;
import io.github.mirvmir.media.api.MediaApi;
import io.github.mirvmir.profile.api.event.ProfileCompletedEvent;
import io.github.mirvmir.profile.api.event.ProfileUpdated;
import io.github.mirvmir.profile.application.service.port.repository.ProfileRepository;
import io.github.mirvmir.profile.application.service.dto.EditProfileRqDto;
import io.github.mirvmir.profile.application.service.interfaces.ProfileService;
import io.github.mirvmir.profile.domain.Profile;
import io.github.mirvmir.profile.application.service.dto.EditProfileRsDto;
import io.github.mirvmir.profile.exception.ProfileErrorCode;
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
    private final MediaApi mediaApi;

    private final ProfileRepository profileRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public EditProfileRsDto editProfile(EditProfileRqDto dto) {
        Long currentUserId = identityApi.getCurrentUserId();

        if (currentUserId == null) {
            throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
        }

        Profile profile = profileRepository.findByUserId(currentUserId);

        boolean wasCompleted  = profile.isCompleted();

        if (dto.avatarFileId() != null && !mediaApi.existsFileById(dto.avatarFileId())) {
            throw new NotFoundException(ProfileErrorCode.MEDIA_FILE_NOT_FOUND);
        }

        profile.change(
                dto.givenName(),
                dto.familyName(),
                dto.fatherName(),
                dto.avatarFileId(),
                dto.emailVisibility()
        );

        profileRepository.save(profile);

        eventPublisher.publishEvent(new ProfileUpdated(
                currentUserId,
                profile.getGivenName(),
                profile.getFamilyName()
        ));

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
    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile() {
        UserInfoDto currentUserInfo = identityApi.getCurrentUserInfo();
        Profile profile = profileRepository.findByUserId(currentUserInfo.userId());

        if (profile == null) {
            profile = Profile.createNew(currentUserInfo.userId());
            profile = profileRepository.save(profile);
        }

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
    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicProfile(Long id) {
        Profile profile = profileRepository.findByUserId(id);

        if (profile == null) {
            throw new NotFoundException(ProfileErrorCode.PROFILE_NOT_FOUND);
        }

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