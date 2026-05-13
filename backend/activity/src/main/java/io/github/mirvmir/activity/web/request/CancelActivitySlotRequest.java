package io.github.mirvmir.activity.web.request;

import jakarta.validation.constraints.NotBlank;

public record CancelActivitySlotRequest(
        @NotBlank
        String reason
) {
}