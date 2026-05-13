package io.github.mirvmir.profile.application.service.dto;

public record EditProfileRsDto(
        boolean profileCompleted,
        String accessToken,
        String refreshToken
) {
}