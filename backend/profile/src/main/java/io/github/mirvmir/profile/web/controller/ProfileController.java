package io.github.mirvmir.profile.web.controller;

import io.github.mirvmir.profile.application.service.dto.EditProfileRqDto;
import io.github.mirvmir.profile.application.service.interfaces.ProfileService;
import io.github.mirvmir.profile.web.request.EditProfileRequest;
import io.github.mirvmir.profile.application.service.dto.EditProfileRsDto;
import io.github.mirvmir.profile.web.response.EditProfileResponse;
import io.github.mirvmir.profile.web.response.MyProfileResponse;
import io.github.mirvmir.profile.web.response.PublicProfileResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public MyProfileResponse getMyProfile() {
        return profileService.getMyProfile();
    }

    @GetMapping("/{id}")
    public PublicProfileResponse getPublicProfile(
            @PathVariable("id")
            Long id
    ) {
        return profileService.getPublicProfile(id);
    }

    @PutMapping("/me")
    public EditProfileResponse editMyProfile(
            @Valid
            @RequestBody
            EditProfileRequest request,
            HttpServletResponse response
    ) {
        EditProfileRsDto rsDto = profileService.editProfile(
                new EditProfileRqDto(
                        request.givenName(),
                        request.familyName(),
                        request.fatherName(),
                        request.avatarFileId(),
                        request.emailVisibility()
                )
        );

        if (rsDto.refreshToken() != null) {
            ResponseCookie refreshCookie = ResponseCookie.from(
                            "refreshToken",
                            rsDto.refreshToken()
                    )
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(Duration.ofDays(30))
                    .build();

            response.addHeader(
                    HttpHeaders.SET_COOKIE,
                    refreshCookie.toString()
            );
        }

        return new EditProfileResponse(
                rsDto.profileCompleted(),
                rsDto.accessToken()
        );
    }
}