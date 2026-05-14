package io.github.mirvmir.identity.web.request;

import jakarta.validation.constraints.NotBlank;

public record RegisterRq(
        @NotBlank
        String email,
        @NotBlank
        String password
) {
}