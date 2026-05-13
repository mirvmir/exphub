package io.github.mirvmir.profile.web.response;

public record EditProfileResponse(
        boolean profileCompleted,
        String accessToken
) {
}
