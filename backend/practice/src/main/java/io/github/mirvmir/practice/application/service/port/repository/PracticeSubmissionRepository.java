package io.github.mirvmir.practice.application.service.port.repository;

import io.github.mirvmir.practice.domain.PracticeSubmission;

import java.util.List;

public interface PracticeSubmissionRepository {
    PracticeSubmission findByLessonIdAndStudentId(Long lessonId, Long studentId);
    List<PracticeSubmission> findByLessonId(Long lessonId);
    PracticeSubmission findById(Long id);
    PracticeSubmission saveOrUpdate(PracticeSubmission submission);
    boolean existsCheckedByLessonIdAndStudentId(Long lessonId, Long studentId);
}