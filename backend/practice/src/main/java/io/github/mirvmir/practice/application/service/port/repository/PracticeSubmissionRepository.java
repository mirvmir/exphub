package io.github.mirvmir.practice.application.service.port.repository;

import io.github.mirvmir.practice.domain.PracticeSubmission;

import java.util.List;
import java.util.UUID;

public interface PracticeSubmissionRepository {
    PracticeSubmission findByStableLessonIdAndStudentId(UUID stableLessonId, Long studentId);
    List<PracticeSubmission> findByLessonId(UUID stableLessonId);
    PracticeSubmission findById(Long id);
    PracticeSubmission saveOrUpdate(PracticeSubmission submission);
    boolean existsCheckedByStableLessonIdAndStudentId(UUID stableLessonId, Long studentId);
}