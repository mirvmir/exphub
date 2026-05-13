package io.github.mirvmir.practice.web.response;

import java.time.Instant;

public record PracticeCommentResponse(
        Long id,
        Long practiceSubmissionAnswerId,
        String html,
        Long fileId,
        Instant createdAt
) {
}