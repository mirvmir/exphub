package io.github.mirvmir.profile.web.response;

public record PublicProfileResponse(
        Long id,
        Long userId,
        String email,
        String givenName,
        String familyName,
        String fatherName,
        Long avatarMediaFileAssetId
) {
}