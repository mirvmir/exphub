package io.github.mirvmir.practice.api;

public interface PracticeApi {
    boolean isPracticeCompletedByLessonIdAndStudentId(Long courseLessonId,
                                                      Long studentId);
}
