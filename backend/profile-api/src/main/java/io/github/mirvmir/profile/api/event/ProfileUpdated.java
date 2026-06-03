package io.github.mirvmir.profile.api.event;

public record ProfileUpdated (
        Long userId,
        String newGivenName,
        String newFamilyName
) {
}