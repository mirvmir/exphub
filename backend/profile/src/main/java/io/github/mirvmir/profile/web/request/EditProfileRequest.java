package io.github.mirvmir.profile.web.request;

import jakarta.validation.constraints.NotBlank;

public record EditProfileRequest(
        @NotBlank
        String givenName,
        @NotBlank
        String familyName,
        String fatherName,
        Long avatarFileId,
        boolean emailVisibility
) {
}