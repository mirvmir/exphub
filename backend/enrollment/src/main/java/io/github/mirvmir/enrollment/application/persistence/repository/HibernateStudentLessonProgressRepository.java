package io.github.mirvmir.enrollment.application.persistence.repository;

import io.github.mirvmir.enrollment.application.service.port.repository.StudentLessonProgressRepository;
import io.github.mirvmir.enrollment.domain.StudentLessonProgress;
import io.github.mirvmir.enrollment.application.persistence.entity.StudentLessonProgressEntity;
import io.github.mirvmir.enrollment.application.persistence.mapper.StudentLessonProgressMapper;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Set;

@AllArgsConstructor
@Repository
public class HibernateStudentLessonProgressRepository implements StudentLessonProgressRepository {

    private final SessionFactory sessionFactory;
    private final StudentLessonProgressMapper userLessonProgressMapper;

    @Override
    public boolean existsByEnrollmentIdAndCourseLessonId(
            Long enrollmentId,
            Long courseLessonId
    ) {
        Boolean exists = sessionFactory.getCurrentSession()
                .createQuery("""
                select count(p.id) > 0
                from StudentLessonProgressEntity p
                where p.enrollmentId = :enrollmentId
                  and p.courseLessonId = :courseLessonId
                """, Boolean.class)
                .setParameter("enrollmentId", enrollmentId)
                .setParameter("courseLessonId", courseLessonId)
                .uniqueResult();

        return Boolean.TRUE.equals(exists);
    }

    @Override
    public StudentLessonProgress saveOrUpdate(StudentLessonProgress progress) {
        Session session = sessionFactory.getCurrentSession();

        StudentLessonProgressEntity entity =
                userLessonProgressMapper.toEntity(progress);

        if (entity.getId() == null) {
            session.persist(entity);
            return userLessonProgressMapper.toDomain(entity);
        }

        StudentLessonProgressEntity merged = session.merge(entity);
        return userLessonProgressMapper.toDomain(merged);
    }

    @Override
    public long countCompletedByEnrollmentIdAndLessonIds(Long enrollmentId,
                                                         Set<Long> lessonIds) {
        if (lessonIds == null || lessonIds.isEmpty()) {
            return 0;
        }

        Long count = sessionFactory.getCurrentSession()
                .createQuery("""
                select count(p.id)
                from StudentLessonProgressEntity p
                where p.enrollmentId = :enrollmentId
                  and p.courseLessonId in :lessonIds
                """, Long.class)
                .setParameter("enrollmentId", enrollmentId)
                .setParameter("lessonIds", lessonIds)
                .uniqueResult();

        return count == null
                ? 0
                : count;
    }
}
