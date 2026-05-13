package io.github.mirvmir.profile.application.service;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.profile.api.dto.CreateProfileRequest;
import io.github.mirvmir.profile.api.dto.ProfileNameDto;
import io.github.mirvmir.profile.application.service.mapper.ProfileNameMapper;
import io.github.mirvmir.profile.application.service.port.repository.ProfileRepository;
import io.github.mirvmir.profile.domain.Profile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultProfileApiTest {

    private ProfileRepository profileRepository;
    private ProfileNameMapper profileNameMapper;
    private DefaultProfileApi api;

    @BeforeEach
    void setUp() {
        profileRepository = mock(ProfileRepository.class);
        profileNameMapper = mock(ProfileNameMapper.class);

        api = new DefaultProfileApi(
                profileRepository,
                profileNameMapper
        );
    }

    @Test
    void getProfileName_shouldReturnProfileName() {
        Profile profile = Profile.load(
                1L,
                10L,
                "Иван",
                "Иванов",
                "Иванович",
                null,
                false
        );

        ProfileNameDto dto = new ProfileNameDto(
                10L,
                "Иван",
                "Иванов"
        );

        when(profileRepository.findByUserId(10L)).thenReturn(profile);
        when(profileNameMapper.toDto(profile)).thenReturn(dto);

        ProfileNameDto response = api.getProfileName(10L);

        assertEquals("Иван", response.givenName());
        assertEquals("Иванов", response.familyName());

        verify(profileRepository).findByUserId(10L);
        verify(profileNameMapper).toDto(profile);
    }

    @Test
    void getProfileName_shouldThrowNotFound_whenProfileNotFound() {
        when(profileRepository.findByUserId(10L)).thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> api.getProfileName(10L)
        );

        assertEquals("Profile with id=10 not found", exception.getMessage());

        verify(profileRepository).findByUserId(10L);
        verifyNoInteractions(profileNameMapper);
    }

    @Test
    void createProfile_shouldCreateAndReturnProfileId() {
        CreateProfileRequest request = new CreateProfileRequest(10L);

        when(profileRepository.save(any(Profile.class)))
                .thenAnswer(invocation -> {
                    Profile profile = invocation.getArgument(0);
                    profile.assignId(1L);
                    return profile;
                });

        Long profileId = api.createProfile(request);

        assertEquals(1L, profileId);

        ArgumentCaptor<Profile> captor =
                ArgumentCaptor.forClass(Profile.class);

        verify(profileRepository).save(captor.capture());

        Profile saved = captor.getValue();

        assertEquals(10L, saved.getUserId());
        assertNull(saved.getGivenName());
        assertNull(saved.getFamilyName());
        assertFalse(saved.isEmailVisibility());
    }

    @Test
    void existsPublicAvatar_shouldReturnTrue_whenAvatarExists() {
        when(profileRepository.existsAvatarByFileId(100L))
                .thenReturn(true);

        boolean result = api.existsPublicAvatar(100L);

        assertTrue(result);

        verify(profileRepository).existsAvatarByFileId(100L);
    }

    @Test
    void existsPublicAvatar_shouldReturnFalse_whenAvatarNotExists() {
        when(profileRepository.existsAvatarByFileId(100L))
                .thenReturn(false);

        boolean result = api.existsPublicAvatar(100L);

        assertFalse(result);

        verify(profileRepository).existsAvatarByFileId(100L);
    }
}