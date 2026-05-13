package io.github.mirvmir.course.application.persistence.repository;

import io.github.mirvmir.course.api.dto.CourseLessonInfoResponse;
import io.github.mirvmir.course.application.service.port.repository.CourseLessonRepository;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@AllArgsConstructor
@Repository
public class HibernateCourseLessonRepository implements CourseLessonRepository {

    private final SessionFactory sessionFactory;

    @Override
    public CourseLessonInfoResponse getLessonInfo(Long courseLessonId) {
        Session session = sessionFactory.getCurrentSession();

        Object[] row = session.createQuery("""
                select
                    c.id,
                    cv.id,
                    cm.id,
                    cl.id,
                    cl.opensAt,
                    cl.isPractice
                from CourseLessonEntity cl
                join cl.courseModule cm
                join cm.courseVersion cv
                join cv.course c
                where cl.id = :courseLessonId
                """, Object[].class)
                .setParameter("courseLessonId", courseLessonId)
                .uniqueResult();

        if (row == null) {
            return null;
        }

        Long courseId = (Long) row[0];
        Long publishedVersionId = (Long) row[1];
        Long courseModuleId = (Long) row[2];
        Long foundLessonId = (Long) row[3];
        Instant opensAt = (Instant) row[4];
        Boolean isPractice = (Boolean) row[5];

        Set<Long> moduleLessonIds = new LinkedHashSet<>(
                        session.createQuery("""
                        select cl.id
                        from CourseLessonEntity cl
                        where cl.courseModule.id = :courseModuleId
                        order by cl.orderIndex
                        """, Long.class)
                        .setParameter("courseModuleId", courseModuleId)
                        .list()
        );

        Set<Long> courseLessonIds = new LinkedHashSet<>(
                        session.createQuery("""
                        select cl.id
                        from CourseLessonEntity cl
                        join cl.courseModule cm
                        where cm.courseVersion.id = :courseVersionId
                        order by cm.orderIndex, cl.orderIndex
                        """, Long.class)
                        .setParameter("courseVersionId", publishedVersionId)
                        .list()
        );

        return new CourseLessonInfoResponse(
                courseId,
                publishedVersionId,
                courseModuleId,
                foundLessonId,
                opensAt,
                Boolean.TRUE.equals(isPractice),
                moduleLessonIds,
                courseLessonIds
        );
    }
}
