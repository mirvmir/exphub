package io.github.mirvmir.practice.web.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PracticeSubmissionDetailsResponse(
        Long id,
        UUID stableLessonId,
        Long courseEnrollmentId,
        Long studentId,
        Instant createdAt,
        Instant checkedAt,
        List<PracticeAnswerDetailsResponse> answers
) {
}