package io.github.mirvmir.profile.application.service.dto;

public record EditProfileRqDto(
        String givenName,
        String familyName,
        String fatherName,
        Long avatarFileId,
        boolean emailVisibility
) {
}