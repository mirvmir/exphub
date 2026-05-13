package io.github.mirvmir.identity.application.service.dto;

import org.springframework.security.core.Authentication;

public record LoginDto(Authentication authentication) {
}
