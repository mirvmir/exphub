package io.github.mirvmir.practice.application.persistence.repository;

import io.github.mirvmir.practice.application.service.port.repository.PracticeSubmissionRepository;
import io.github.mirvmir.practice.domain.PracticeSubmission;
import io.github.mirvmir.practice.application.persistence.entity.PracticeSubmissionEntity;
import io.github.mirvmir.practice.application.persistence.mapper.PracticeSubmissionMapper;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Repository
public class HibernatePracticeSubmissionRepository implements PracticeSubmissionRepository {

    private final SessionFactory sessionFactory;
    private final PracticeSubmissionMapper practiceSubmissionMapper;

    @Override
    public PracticeSubmission findByStableLessonIdAndStudentId(UUID stableLessonId,
                                                               Long studentId) {
        PracticeSubmissionEntity entity = sessionFactory.getCurrentSession()
                .createQuery("""
                select ps
                from PracticeSubmissionEntity ps
                where ps.stableLessonId = :stableLessonId
                  and ps.studentId = :studentId
                """, PracticeSubmissionEntity.class)
                .setParameter("stableLessonId", stableLessonId)
                .setParameter("studentId", studentId)
                .uniqueResult();

        return practiceSubmissionMapper.toDomain(entity);
    }

    @Override
    public List<PracticeSubmission> findByLessonId(UUID stableLessonId) {
        Session session = sessionFactory.getCurrentSession();

        return session.createQuery("""
                from PracticeSubmissionEntity ps
                where ps.stableLessonId = :stableLessonId
                order by ps.createdAt asc
                """, PracticeSubmissionEntity.class)
                .setParameter("stableLessonId", stableLessonId)
                .getResultList()
                .stream()
                .map(practiceSubmissionMapper::toDomain)
                .toList();
    }

    @Override
    public PracticeSubmission findById(Long id) {
        Session session = sessionFactory.getCurrentSession();

        PracticeSubmissionEntity entity = session.get(
                PracticeSubmissionEntity.class,
                id
        );

        return practiceSubmissionMapper.toDomain(entity);
    }

    @Override
    public PracticeSubmission saveOrUpdate(PracticeSubmission submission) {
        Session session = sessionFactory.getCurrentSession();

        PracticeSubmissionEntity entity =
                practiceSubmissionMapper.toEntity(submission);

        if (entity.getId() == null) {
            session.persist(entity);
            submission.assignId(entity.getId());
            return submission;
        }

        PracticeSubmissionEntity merged = session.merge(entity);

        return practiceSubmissionMapper.toDomain(merged);
    }

    @Override
    public boolean existsCheckedByStableLessonIdAndStudentId(UUID stableLessonId,
                                                             Long studentId) {
        Boolean exists = sessionFactory.getCurrentSession()
                .createQuery("""
                select count(ps.id) > 0
                from PracticeSubmissionEntity ps
                where ps.stableLessonId = :stableLessonId
                  and ps.studentId = :studentId
                  and ps.checkedAt is not null
                """, Boolean.class)
                .setParameter("stableLessonId", stableLessonId)
                .setParameter("studentId", studentId)
                .uniqueResult();

        return Boolean.TRUE.equals(exists);
    }
}