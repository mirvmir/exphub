package io.github.mirvmir.activity.web.request;

import jakarta.validation.constraints.NotBlank;

public record RejectActivityRequest(
        @NotBlank
        String moderationComment
) {
}