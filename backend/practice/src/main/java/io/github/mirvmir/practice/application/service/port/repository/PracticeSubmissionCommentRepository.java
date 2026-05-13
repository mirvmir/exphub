package io.github.mirvmir.practice.application.service.port.repository;

import io.github.mirvmir.practice.domain.PracticeSubmissionComment;

import java.util.Collection;
import java.util.List;

public interface PracticeSubmissionCommentRepository {
    List<PracticeSubmissionComment> findByPracticeSubmissionAnswerIds(Collection<Long> answerIds);
    PracticeSubmissionComment saveOrUpdate(PracticeSubmissionComment comment);
}