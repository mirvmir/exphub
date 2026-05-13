package io.github.mirvmir.identity.application.service.dto;

import org.springframework.security.core.Authentication;

public record RegisterDto(Authentication authentication) {
}
