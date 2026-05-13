package io.github.mirvmir.profile.web.controller;

import io.github.mirvmir.common.exception.GlobalExceptionHandler;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.profile.application.service.dto.EditProfileRsDto;
import io.github.mirvmir.profile.application.service.interfaces.ProfileService;
import io.github.mirvmir.profile.exception.ProfileErrorCode;
import io.github.mirvmir.profile.web.response.MyProfileResponse;
import io.github.mirvmir.profile.web.response.PublicProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class ProfileControllerTest {

    private ProfileService profileService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        profileService = mock(ProfileService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProfileController(profileService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getMyProfile_shouldReturn200() throws Exception {
        MyProfileResponse response = new MyProfileResponse(
                1L,
                10L,
                "user@mail.ru",
                "Иван",
                "Иванов",
                "Иванович",
                100L,
                true
        );

        when(profileService.getMyProfile()).thenReturn(response);

        mockMvc.perform(get("/profile/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(10L))
                .andExpect(jsonPath("$.email").value("user@mail.ru"))
                .andExpect(jsonPath("$.givenName").value("Иван"))
                .andExpect(jsonPath("$.familyName").value("Иванов"))
                .andExpect(jsonPath("$.fatherName").value("Иванович"))
                .andExpect(jsonPath("$.avatarMediaFileAssetId").value(100L))
                .andExpect(jsonPath("$.completed").value(true));

        verify(profileService).getMyProfile();
    }

    @Test
    void getPublicProfile_shouldReturn200() throws Exception {
        PublicProfileResponse response = new PublicProfileResponse(
                1L,
                10L,
                "user@mail.ru",
                "Иван",
                "Иванов",
                "Иванович",
                100L
        );

        when(profileService.getPublicProfile(10L)).thenReturn(response);

        mockMvc.perform(get("/profile/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(10L))
                .andExpect(jsonPath("$.email").value("user@mail.ru"))
                .andExpect(jsonPath("$.givenName").value("Иван"))
                .andExpect(jsonPath("$.familyName").value("Иванов"))
                .andExpect(jsonPath("$.fatherName").value("Иванович"))
                .andExpect(jsonPath("$.avatarMediaFileAssetId").value(100L));

        verify(profileService).getPublicProfile(10L);
    }

    @Test
    void getPublicProfile_shouldReturn404_whenProfileNotFound() throws Exception {
        when(profileService.getPublicProfile(10L))
                .thenThrow(new NotFoundException(
                        ProfileErrorCode.PROFILE_NOT_FOUND,
                        "Profile with id=10 not found"
                ));

        mockMvc.perform(get("/profile/{id}", 10L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROFILE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Profile with id=10 not found"));
    }

    @Test
    void editMyProfile_shouldReturn200WithoutTokens_whenProfileAlreadyCompleted() throws Exception {
        when(profileService.editProfile(any()))
                .thenReturn(new EditProfileRsDto(
                        true,
                        null,
                        null
                ));

        mockMvc.perform(put("/profile/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "givenName": "Иван",
                                  "familyName": "Иванов",
                                  "fatherName": "Иванович",
                                  "avatarFileId": 100,
                                  "emailVisibility": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.profileCompleted").value(true))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));

        ArgumentCaptor<io.github.mirvmir.profile.application.service.dto.EditProfileRqDto> captor =
                ArgumentCaptor.forClass(io.github.mirvmir.profile.application.service.dto.EditProfileRqDto.class);

        verify(profileService).editProfile(captor.capture());

        assertEquals("Иван", captor.getValue().givenName());
        assertEquals("Иванов", captor.getValue().familyName());
        assertEquals("Иванович", captor.getValue().fatherName());
        assertEquals(100L, captor.getValue().avatarFileId());
        assertEquals(true, captor.getValue().emailVisibility());
    }

    @Test
    void editMyProfile_shouldReturn200AndSetRefreshTokenCookie_whenProfileCompletedFirstTime() throws Exception {
        when(profileService.editProfile(any()))
                .thenReturn(new EditProfileRsDto(
                        true,
                        "access-token",
                        "refresh-token"
                ));

        mockMvc.perform(put("/profile/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "givenName": "Иван",
                                  "familyName": "Иванов",
                                  "fatherName": null,
                                  "avatarFileId": null,
                                  "emailVisibility": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileCompleted").value(true))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.containsString("refreshToken=refresh-token")
                ));
    }

    @Test
    void editMyProfile_shouldReturn400_whenGivenNameIsBlank() throws Exception {
        mockMvc.perform(put("/profile/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "givenName": "",
                                  "familyName": "Иванов",
                                  "fatherName": null,
                                  "avatarFileId": null,
                                  "emailVisibility": true
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(profileService);
    }

    @Test
    void getMyProfile_shouldReturn503_whenDatabaseUnavailable() throws Exception {
        when(profileService.getMyProfile())
                .thenThrow(new CannotGetJdbcConnectionException(
                        "Database unavailable"
                ));

        mockMvc.perform(get("/profile/me"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("DATABASE_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("Временная ошибка подключения к базе данных"));
    }

    @Test
    void getMyProfile_shouldReturn500_whenDatabaseError() throws Exception {
        when(profileService.getMyProfile())
                .thenThrow(new DataAccessResourceFailureException(
                        "Database error"
                ));

        mockMvc.perform(get("/profile/me"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("DATABASE_ERROR"))
                .andExpect(jsonPath("$.message").value("Ошибка при обращении к базе данных"));
    }
}