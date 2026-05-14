package io.github.mirvmir.identity.web.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRq(
        @NotBlank
        String email,
        @NotBlank
        String password
) {
}
