package io.github.mirvmir.profile.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EditProfileRequest(
        @NotBlank
        @Size(max = 250)
        String givenName,
        @NotBlank
        @Size(max = 250)
        String familyName,
        @Size(max = 250)
        String fatherName,
        @Positive
        Long avatarFileId,
        boolean emailVisibility
) {
}