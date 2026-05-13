package io.github.mirvmir.practice.web.response;

import java.time.Instant;
import java.util.List;

public record PracticeSubmissionDetailsResponse(
        Long id,
        Long lessonId,
        Long courseEnrollmentId,
        Long studentId,
        Instant createdAt,
        Instant checkedAt,
        List<PracticeAnswerDetailsResponse> answers
) {
}