package io.github.mirvmir.practice.application.service.port.repository;

import io.github.mirvmir.practice.domain.PracticeSubmissionAnswer;

import java.util.Collection;
import java.util.List;

public interface PracticeSubmissionAnswerRepository {
    PracticeSubmissionAnswer findById(Long id);
    List<PracticeSubmissionAnswer> findByPracticeSubmissionId(Long practiceSubmissionId);
    List<PracticeSubmissionAnswer> findByPracticeSubmissionIds(Collection<Long> practiceSubmissionIds);
    PracticeSubmissionAnswer saveOrUpdate(PracticeSubmissionAnswer answer);
}