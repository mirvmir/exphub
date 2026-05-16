package io.github.mirvmir.activity.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelActivitySlotRequest(
        @NotBlank
        @Size(max = 255)
        String reason
) {
}