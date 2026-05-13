package io.github.mirvmir.practice.web.response;

import java.time.Instant;

public record PracticeAnswerResponse(
        Long id,
        Long practiceSubmissionId,
        String html,
        Long fileId,
        Instant createdAt
) {
}