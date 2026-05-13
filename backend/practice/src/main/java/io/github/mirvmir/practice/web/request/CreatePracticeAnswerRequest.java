package io.github.mirvmir.practice.web.request;

import jakarta.validation.constraints.NotBlank;

public record CreatePracticeAnswerRequest(
        @NotBlank
        String html,
        Long fileId
) {
}
