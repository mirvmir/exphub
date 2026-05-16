package io.github.mirvmir.profile.web.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.NonNull;

public record MyProfileResponse(
        Long id,
        @NonNull
        Long userId,
        @NotBlank
        @Email
        String email,
        @Size(max = 250)
        String givenName,
        @Size(max = 250)
        String familyName,
        @Size(max = 250)
        String fatherName,
        @Positive
        Long avatarMediaFileAssetId,
        boolean completed
) {
}