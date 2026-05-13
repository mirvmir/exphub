package io.github.mirvmir.practice.web.request;

import jakarta.validation.constraints.NotBlank;

public record CreatePracticeCommentRequest(
        @NotBlank String html,
        Long fileId
) {
}