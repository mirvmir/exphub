package io.github.mirvmir.practice.web.response;

import java.time.Instant;
import java.util.UUID;

public record PracticeSubmissionResponse(
        Long id,
        UUID stableLessonId,
        Long courseEnrollmentId,
        Long studentId,
        Instant createdAt,
        Instant checkedAt
) {
}