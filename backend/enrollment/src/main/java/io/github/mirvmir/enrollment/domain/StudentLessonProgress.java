package io.github.mirvmir.enrollment.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class StudentLessonProgress {

    private Long id;
    @NonNull
    private Long enrollmentId;
    @NonNull
    private Long courseLessonId;
    @NonNull
    private Instant completedAt;

    public static StudentLessonProgress create(
            Long enrollmentId,
            Long courseLessonId,
            Instant now
    ) {
        return new StudentLessonProgress(
                null,
                enrollmentId,
                courseLessonId,
                now
        );
    }

    public static StudentLessonProgress load(
            Long id,
            Long enrollmentId,
            Long courseLessonId,
            Instant completedAt
    ) {
        return new StudentLessonProgress(
                id,
                enrollmentId,
                courseLessonId,
                completedAt
        );
    }

    public void assignId(Long id) {
        this.id = id;
    }
}