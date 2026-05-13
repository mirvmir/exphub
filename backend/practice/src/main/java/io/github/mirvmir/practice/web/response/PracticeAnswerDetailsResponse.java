package io.github.mirvmir.practice.web.response;

import java.time.Instant;
import java.util.List;

public record PracticeAnswerDetailsResponse(
        Long id,
        Long practiceSubmissionId,
        String html,
        Long fileId,
        Instant createdAt,
        List<PracticeCommentResponse> comments
) {
}