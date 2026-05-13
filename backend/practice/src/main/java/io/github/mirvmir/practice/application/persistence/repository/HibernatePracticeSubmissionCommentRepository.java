package io.github.mirvmir.practice.application.persistence.repository;

import io.github.mirvmir.practice.application.service.port.repository.PracticeSubmissionCommentRepository;
import io.github.mirvmir.practice.domain.PracticeSubmissionComment;
import io.github.mirvmir.practice.application.persistence.entity.PracticeSubmissionCommentEntity;
import io.github.mirvmir.practice.application.persistence.mapper.PracticeSubmissionCommentMapper;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@Repository
public class HibernatePracticeSubmissionCommentRepository implements PracticeSubmissionCommentRepository {

    private final SessionFactory sessionFactory;
    private final PracticeSubmissionCommentMapper practiceSubmissionCommentMapper;

    @Override
    public List<PracticeSubmissionComment> findByPracticeSubmissionAnswerIds(Collection<Long> answerIds) {
        if (answerIds == null || answerIds.isEmpty()) {
            return List.of();
        }

        Session session = sessionFactory.getCurrentSession();

        return session.createQuery("""
                from PracticeSubmissionCommentEntity psc
                where psc.practiceSubmissionAnswerId in :answerIds
                order by psc.createdAt asc
                """, PracticeSubmissionCommentEntity.class)
                .setParameter("answerIds", answerIds)
                .getResultList()
                .stream()
                .map(practiceSubmissionCommentMapper::toDomain)
                .toList();
    }

    @Override
    public PracticeSubmissionComment saveOrUpdate(PracticeSubmissionComment comment) {
        Session session = sessionFactory.getCurrentSession();

        PracticeSubmissionCommentEntity entity =
                practiceSubmissionCommentMapper.toEntity(comment);

        if (entity.getId() == null) {
            session.persist(entity);
            comment.assignId(entity.getId());
            return comment;
        }

        PracticeSubmissionCommentEntity merged = session.merge(entity);

        return practiceSubmissionCommentMapper.toDomain(merged);
    }
}