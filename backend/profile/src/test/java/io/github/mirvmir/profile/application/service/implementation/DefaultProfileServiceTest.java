package io.github.mirvmir.profile.application.service.implementation;

import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.identity.dto.TokenDto;
import io.github.mirvmir.identity.dto.UserInfoDto;
import io.github.mirvmir.media.api.MediaApi;
import io.github.mirvmir.profile.api.event.ProfileCompletedEvent;
import io.github.mirvmir.profile.application.service.dto.EditProfileRqDto;
import io.github.mirvmir.profile.application.service.dto.EditProfileRsDto;
import io.github.mirvmir.profile.application.service.port.repository.ProfileRepository;
import io.github.mirvmir.profile.domain.Profile;
import io.github.mirvmir.profile.web.response.MyProfileResponse;
import io.github.mirvmir.profile.web.response.PublicProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultProfileServiceTest {

    private MediaApi mediaApi;
    private IdentityApi identityApi;
    private ProfileRepository profileRepository;
    private ApplicationEventPublisher eventPublisher;
    private DefaultProfileService service;

    @BeforeEach
    void setUp() {
        mediaApi = mock(MediaApi.class);
        identityApi = mock(IdentityApi.class);
        profileRepository = mock(ProfileRepository.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        service = new DefaultProfileService(
                identityApi,
                mediaApi,
                profileRepository,
                eventPublisher
        );
    }

    @Test
    void editProfile_shouldSaveProfileWithoutTokens_whenProfileWasAlreadyCompleted() {
        Profile profile = Profile.load(
                1L,
                10L,
                "Иван",
                "Иванов",
                null,
                null,
                false
        );

        EditProfileRqDto request = new EditProfileRqDto(
                "Петр",
                "Петров",
                "Петрович",
                100L,
                true
        );

        when(identityApi.getCurrentUserId()).thenReturn(10L);
        when(profileRepository.findByUserId(10L)).thenReturn(profile);
        when(mediaApi.existsFileById(100L)).thenReturn(true);

        EditProfileRsDto response = service.editProfile(request);

        assertTrue(response.profileCompleted());
        assertNull(response.accessToken());
        assertNull(response.refreshToken());

        assertEquals("Петр", profile.getGivenName());
        assertEquals("Петров", profile.getFamilyName());
        assertEquals("Петрович", profile.getFatherName());
        assertEquals(100L, profile.getAvatarFileId());
        assertTrue(profile.isEmailVisibility());

        verify(profileRepository).save(profile);
        verifyNoInteractions(eventPublisher);
        verify(identityApi, never()).reissueTokens(anyLong());
    }

    @Test
    void editProfile_shouldPublishEventAndReissueTokens_whenProfileCompletedFirstTime() {
        Profile profile = Profile.load(
                1L,
                10L,
                null,
                null,
                null,
                null,
                false
        );

        EditProfileRqDto request = new EditProfileRqDto(
                "Иван",
                "Иванов",
                null,
                null,
                false
        );

        TokenDto tokenDto = new TokenDto(
                "access-token",
                "refresh-token"
        );

        when(identityApi.getCurrentUserId()).thenReturn(10L);
        when(profileRepository.findByUserId(10L)).thenReturn(profile);
        when(identityApi.reissueTokens(10L)).thenReturn(tokenDto);

        EditProfileRsDto response = service.editProfile(request);

        assertTrue(response.profileCompleted());
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());

        verify(profileRepository).save(profile);

        ArgumentCaptor<ProfileCompletedEvent> captor =
                ArgumentCaptor.forClass(ProfileCompletedEvent.class);

        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(10L, captor.getValue().userId());

        verify(identityApi).reissueTokens(10L);
    }

    @Test
    void editProfile_shouldReturnNotCompleted_whenRequiredNamesAreBlank() {
        Profile profile = Profile.load(
                1L,
                10L,
                null,
                null,
                null,
                null,
                false
        );

        EditProfileRqDto request = new EditProfileRqDto(
                "",
                "",
                null,
                null,
                false
        );

        when(identityApi.getCurrentUserId()).thenReturn(10L);
        when(profileRepository.findByUserId(10L)).thenReturn(profile);

        EditProfileRsDto response = service.editProfile(request);

        assertFalse(response.profileCompleted());
        assertNull(response.accessToken());
        assertNull(response.refreshToken());

        verify(profileRepository).save(profile);
        verifyNoInteractions(eventPublisher);
        verify(identityApi, never()).reissueTokens(anyLong());
    }

    @Test
    void getMyProfile_shouldReturnCurrentUserProfile() {
        UserInfoDto userInfo = new UserInfoDto(
                10L,
                "user@mail.ru"
        );

        Profile profile = Profile.load(
                1L,
                10L,
                "Иван",
                "Иванов",
                "Иванович",
                100L,
                true
        );

        when(identityApi.getCurrentUserInfo()).thenReturn(userInfo);
        when(profileRepository.findByUserId(10L)).thenReturn(profile);

        MyProfileResponse response = service.getMyProfile();

        assertEquals(1L, response.id());
        assertEquals(10L, response.userId());
        assertEquals("user@mail.ru", response.email());
        assertEquals("Иван", response.givenName());
        assertEquals("Иванов", response.familyName());
        assertEquals("Иванович", response.fatherName());
        assertEquals(100L, response.avatarMediaFileAssetId());
        assertTrue(response.completed());
    }

    @Test
    void getPublicProfile_shouldReturnEmail_whenEmailVisibilityTrue() {
        UserInfoDto userInfo = new UserInfoDto(
                10L,
                "user@mail.ru"
        );

        Profile profile = Profile.load(
                1L,
                10L,
                "Иван",
                "Иванов",
                null,
                100L,
                true
        );

        when(identityApi.getUserInfoById(10L)).thenReturn(userInfo);
        when(profileRepository.findByUserId(10L)).thenReturn(profile);

        PublicProfileResponse response = service.getPublicProfile(10L);

        assertEquals(1L, response.id());
        assertEquals(10L, response.userId());
        assertEquals("user@mail.ru", response.email());
        assertEquals("Иван", response.givenName());
        assertEquals("Иванов", response.familyName());
        assertEquals(100L, response.avatarMediaFileAssetId());
    }

    @Test
    void getPublicProfile_shouldHideEmail_whenEmailVisibilityFalse() {
        UserInfoDto userInfo = new UserInfoDto(
                10L,
                "user@mail.ru"
        );

        Profile profile = Profile.load(
                1L,
                10L,
                "Иван",
                "Иванов",
                null,
                100L,
                false
        );

        when(identityApi.getUserInfoById(10L)).thenReturn(userInfo);
        when(profileRepository.findByUserId(10L)).thenReturn(profile);

        PublicProfileResponse response = service.getPublicProfile(10L);

        assertNull(response.email());
    }
}