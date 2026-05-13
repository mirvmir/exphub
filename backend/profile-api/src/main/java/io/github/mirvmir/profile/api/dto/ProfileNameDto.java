package io.github.mirvmir.profile.api.dto;

public record ProfileNameDto(
        Long userId,
        String givenName,
        String familyName
) {
}
