package io.github.mirvmir.practice.api;

import java.util.UUID;

public interface PracticeApi {
    boolean isPracticeCompletedByLessonIdAndStudentId(UUID stableLessonId,
                                                      Long studentId);
}
