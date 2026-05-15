package io.github.mirvmir.course.application.persistence.repository;

import io.github.mirvmir.course.application.service.port.repository.CourseVersionRepository;
import io.github.mirvmir.course.domain.CourseVersion;
import io.github.mirvmir.course.application.persistence.entity.CourseVersionEntity;
import io.github.mirvmir.course.application.persistence.mapper.CourseVersionMapper;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
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
    public CourseVersion findById(Long versionId) {
        Session session = sessionFactory.getCurrentSession();

        CourseVersionEntity entity = session.find(
                CourseVersionEntity.class,
                versionId
        );

        return entity == null
                ? null
                : courseVersionMapper.toDomain(entity);
    }

    @Override
    public CourseVersion findByIdWithModules(Long versionId) {
        Session session = sessionFactory.getCurrentSession();

        CourseVersionEntity entity = session.createQuery("""
                select distinct v
                from CourseVersionEntity v
                left join fetch v.modules m
                where v.id = :versionId
                """, CourseVersionEntity.class)
                .setParameter("versionId", versionId)
                .uniqueResult();

        return entity == null
                ? null
                : courseVersionMapper.toDomain(entity);
    }

    @Override
    public CourseVersion findByIdWithModule(Long versionId,
                                            UUID stableModuleId) {
        Session session = sessionFactory.getCurrentSession();

        CourseVersionEntity entity = session.find(
                CourseVersionEntity.class,
                versionId
        );

        if (entity == null) {
            return null;
        }

        Hibernate.initialize(entity.getModules());
        entity.getModules()
                .stream()
                .filter(module -> stableModuleId.equals(module.getStableModuleId()))
                .findFirst()
                .ifPresent(module -> Hibernate.initialize(module.getLessons()));

        return courseVersionMapper.toDomain(entity);
    }

    @Override
    public CourseVersion findByIdWithLesson(Long versionId,
                                            UUID stableLessonId) {
        Session session = sessionFactory.getCurrentSession();

        CourseVersionEntity entity = session.find(
                CourseVersionEntity.class,
                versionId
        );

        if (entity == null) {
            return null;
        }

        Hibernate.initialize(entity.getModules());
        entity.getModules().forEach(module -> {
            Hibernate.initialize(module.getLessons());

            module.getLessons()
                    .stream()
                    .filter(lesson -> stableLessonId.equals(lesson.getStableLessonId()))
                    .findFirst()
                    .ifPresent(lesson -> Hibernate.initialize(lesson.getBlocks()));
        });

        return courseVersionMapper.toDomain(entity);
    }

    @Override
    public void updateModerationState(CourseVersion version) {
        Session session = sessionFactory.getCurrentSession();

        session.createMutationQuery("""
                update CourseVersionEntity v
                set v.moderationStatus = :moderationStatus,
                    v.moderationComment = :moderationComment
                where v.id = :versionId
                """)
                .setParameter("moderationStatus", version.getStatus())
                .setParameter("moderationComment", version.getModerationComment())
                .setParameter("versionId", version.getId())
                .executeUpdate();
    }
}
