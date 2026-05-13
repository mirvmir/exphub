package io.github.mirvmir.enrollment.application.service.port.repository;

import io.github.mirvmir.enrollment.domain.StudentLessonProgress;

import java.util.Set;

public interface StudentLessonProgressRepository {
    boolean existsByEnrollmentIdAndCourseLessonId(Long enrollmentId,
                                                  Long courseLessonId);
    StudentLessonProgress saveOrUpdate(StudentLessonProgress progress);
    long countCompletedByEnrollmentIdAndLessonIds(Long enrollmentId,
                                                  Set<Long> lessonIds);
}
