package io.github.mirvmir.identity.application.service.dto;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public record RefreshDto(
        Long userId,
        Collection<? extends GrantedAuthority> authorities,
        boolean isProfileCompleted
) {
}
