package io.github.mirvmir.course.application.persistence.repository;

import io.github.mirvmir.course.application.service.port.repository.CourseVersionRepository;
import io.github.mirvmir.course.domain.CourseVersion;
import io.github.mirvmir.course.application.persistence.entity.CourseVersionEntity;
import io.github.mirvmir.course.application.persistence.mapper.CourseVersionMapper;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@AllArgsConstructor
@Repository
public class HibernateCourseVersionRepository implements CourseVersionRepository {

    private final SessionFactory sessionFactory;
    private final CourseVersionMapper courseVersionMapper;

    @Override
    public CourseVersion findByIdAndCourseId(Long versionId,
                                             Long courseId) {
        Session session = sessionFactory.getCurrentSession();

        CourseVersionEntity entity = session.createQuery("""
                select v
                from CourseVersionEntity v
                where v.id = :versionId
                  and exists (
                        select c.id
                        from CourseEntity c
                        where c.id = :courseId
                          and (
                                c.draftVersion = v
                                or c.publishedVersion = v
                          )
                  )
                """, CourseVersionEntity.class)
                .setParameter("versionId", versionId)
                .setParameter("courseId", courseId)
                .uniqueResult();

        return entity == null
                ? null
                : courseVersionMapper.toDomain(entity);
    }

    @Override
    public CourseVersion findByIdAndCourseIdWithModules(Long versionId,
                                                        Long courseId) {
        Session session = sessionFactory.getCurrentSession();

        CourseVersionEntity entity = session.createQuery("""
                select distinct v
                from CourseVersionEntity v
                left join fetch v.modules m
                where v.id = :versionId
                  and exists (
                        select c.id
                        from CourseEntity c
                        where c.id = :courseId
                          and (
                                c.draftVersion = v
                                  or c.publishedVersion = v
                                )
                  )
                """, CourseVersionEntity.class)
                .setParameter("versionId", versionId)
                .setParameter("courseId", courseId)
                .uniqueResult();

        return entity == null
                ? null
                : courseVersionMapper.toDomain(entity);
    }

    @Override
    public CourseVersion findByIdAndCourseIdWithModule(Long versionId,
                                                       Long courseId,
                                                       UUID stableModuleId) {
        Session session = sessionFactory.getCurrentSession();

        CourseVersionEntity entity = session.createQuery("""
                select distinct v
                from CourseVersionEntity v
                join fetch v.modules m
                left join fetch m.lessons l
                where v.id = :versionId
                  and m.stableModuleId = :stableModuleId
                  and exists (
                        select c.id
                        from CourseEntity c
                        where c.id = :courseId
                          and (
                                c.draftVersion = v
                                or c.publishedVersion = v
                          )
                  )
                """, CourseVersionEntity.class)
                .setParameter("versionId", versionId)
                .setParameter("courseId", courseId)
                .setParameter("stableModuleId", stableModuleId)
                .uniqueResult();

        return entity == null
                ? null
                : courseVersionMapper.toDomain(entity);
    }

    @Override
    public CourseVersion findByIdAndCourseIdWithLesson(
            Long versionId,
            Long courseId,
            UUID stableLessonId
    ) {
        Session session = sessionFactory.getCurrentSession();

        CourseVersionEntity entity = session.createQuery("""
                select distinct v
                from CourseVersionEntity v
                join fetch v.modules m
                join fetch m.lessons l
                left join fetch l.blocks b
                where v.id = :versionId
                  and l.stableLessonId = :stableLessonId
                  and exists (
                     select c.id
                     from CourseEntity c
                     where c.id = :courseId
                       and (
                            c.draftVersion = v
                            or c.publishedVersion = v
                       )
                  )
                """, CourseVersionEntity.class)
                .setParameter("versionId", versionId)
                .setParameter("courseId", courseId)
                .setParameter("stableLessonId", stableLessonId)
                .uniqueResult();

        return entity == null
                ? null
                : courseVersionMapper.toDomain(entity);
    }
}
