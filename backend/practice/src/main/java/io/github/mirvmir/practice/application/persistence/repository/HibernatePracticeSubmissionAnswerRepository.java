package io.github.mirvmir.practice.application.persistence.repository;

import io.github.mirvmir.practice.application.service.port.repository.PracticeSubmissionAnswerRepository;
import io.github.mirvmir.practice.domain.PracticeSubmissionAnswer;
import io.github.mirvmir.practice.application.persistence.entity.PracticeSubmissionAnswerEntity;
import io.github.mirvmir.practice.application.persistence.mapper.PracticeSubmissionAnswerMapper;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@Repository
public class HibernatePracticeSubmissionAnswerRepository implements PracticeSubmissionAnswerRepository {

    private final SessionFactory sessionFactory;
    private final PracticeSubmissionAnswerMapper practiceSubmissionAnswerMapper;

    @Override
    public PracticeSubmissionAnswer findById(Long id) {
        Session session = sessionFactory.getCurrentSession();

        PracticeSubmissionAnswerEntity entity = session.get(
                PracticeSubmissionAnswerEntity.class,
                id
        );

        return practiceSubmissionAnswerMapper.toDomain(entity);
    }

    @Override
    public List<PracticeSubmissionAnswer> findByPracticeSubmissionId(Long practiceSubmissionId) {
        Session session = sessionFactory.getCurrentSession();

        return session.createQuery("""
                from PracticeSubmissionAnswerEntity psa
                where psa.practiceSubmissionId = :practiceSubmissionId
                order by psa.createdAt asc
                """, PracticeSubmissionAnswerEntity.class)
                .setParameter("practiceSubmissionId", practiceSubmissionId)
                .getResultList()
                .stream()
                .map(practiceSubmissionAnswerMapper::toDomain)
                .toList();
    }

    @Override
    public List<PracticeSubmissionAnswer> findByPracticeSubmissionIds(Collection<Long> practiceSubmissionIds) {
        if (practiceSubmissionIds == null || practiceSubmissionIds.isEmpty()) {
            return List.of();
        }

        Session session = sessionFactory.getCurrentSession();

        return session.createQuery("""
                from PracticeSubmissionAnswerEntity psa
                where psa.practiceSubmissionId in :practiceSubmissionIds
                order by psa.createdAt asc
                """, PracticeSubmissionAnswerEntity.class)
                    .setParameter("practiceSubmissionIds", practiceSubmissionIds)
                .getResultList()
                .stream()
                .map(practiceSubmissionAnswerMapper::toDomain)
                .toList();
    }

    @Override
    public PracticeSubmissionAnswer saveOrUpdate(PracticeSubmissionAnswer answer) {
        Session session = sessionFactory.getCurrentSession();

        PracticeSubmissionAnswerEntity entity =
                practiceSubmissionAnswerMapper.toEntity(answer);

        if (entity.getId() == null) {
            session.persist(entity);
            answer.assignId(entity.getId());
            return answer;
        }

        PracticeSubmissionAnswerEntity merged = session.merge(entity);

        return practiceSubmissionAnswerMapper.toDomain(merged);
    }
}