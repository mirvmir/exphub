package io.github.mirvmir.practice.web.response;

import java.time.Instant;

public record PracticeSubmissionResponse(
        Long id,
        Long lessonId,
        Long courseEnrollmentId,
        Long studentId,
        Instant createdAt,
        Instant checkedAt
) {
}