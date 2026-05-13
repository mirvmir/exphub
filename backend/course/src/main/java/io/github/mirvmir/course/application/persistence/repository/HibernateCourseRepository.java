package io.github.mirvmir.course.application.persistence.repository;

import io.github.mirvmir.course.application.service.port.repository.CourseRepository;
import io.github.mirvmir.course.domain.Course;
import io.github.mirvmir.course.application.persistence.entity.CourseEntity;
import io.github.mirvmir.course.application.persistence.mapper.CourseMapper;
import io.github.mirvmir.course.domain.LessonType;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Repository
public class HibernateCourseRepository implements CourseRepository {

    private final SessionFactory sessionFactory;
    private final CourseMapper courseMapper;

    @Override
    public Course saveOrUpdate(Course course) {
        Session session = sessionFactory.getCurrentSession();

        CourseEntity entity = courseMapper.toEntity(course);
        entity.replaceTopics(course.getTopicIds());
        entity.replaceLessonOpenings(course.getLessonOpenings());

        if (entity.getId() == null) {
            session.persist(entity);
            session.flush();

            course.assignId(entity.getId());
            return course;
        }

        CourseEntity merged = session.merge(entity);

        return courseMapper.toDomain(merged);
    }

    @Override
    public Course findById(Long id) {
        Session session = sessionFactory.getCurrentSession();

        CourseEntity entity = session.createQuery("""
                 select distinct c
                 from CourseEntity c
                 left join fetch c.draftVersion
                 left join fetch c.publishedVersion
                 left join fetch c.topicEntities
                 left join fetch c.lessonOpeningEntities
                 where c.id = :id
                 """, CourseEntity.class)
                .setParameter("id", id)
                .uniqueResult();

        if (entity == null) {
            return null;
        }

        return courseMapper.toDomain(entity);
    }

    @Override
    public Course findByIdWithDraftContent(Long id) {
        return findByIdWithVersionContent(id, true);
    }

    @Override
    public Course findByIdWithPublishedContent(Long id) {
        return findByIdWithVersionContent(id, false);
    }

    @Override
    public Course findByIdWithSettings(Long id) {
        Session session = sessionFactory.getCurrentSession();

        CourseEntity entity = session.createQuery("""
                select distinct c
                from CourseEntity c
                left join fetch c.topicEntities
                left join fetch c.lessonOpeningEntities
                where c.id = :id
                """, CourseEntity.class)
                .setParameter("id", id)
                .uniqueResult();

        return entity == null
                ? null
                : courseMapper.toDomain(entity);
    }

    @Override
    public boolean canTeacherAccessFile(Long userId, Long fileId) {
        Session session = sessionFactory.getCurrentSession();

        return Boolean.TRUE.equals(
                session.createQuery("""
                        select count(c.id) > 0
                        from FileLessonEntity fl
                        join fl.lessonBlock lb
                        join lb.lesson l
                        join l.module m
                        join m.courseVersion cv
                        join CourseEntity c
                          on c.publishedVersion.id = cv.id
                          or c.draftVersion.id = cv.id
                        where fl.fileAssetId = :fileId
                          and c.authorId = :userId
                        """, Boolean.class)
                        .setParameter("fileId", fileId)
                        .setParameter("userId", userId)
                        .uniqueResult()
        );
    }

    @Override
    public boolean canTeacherAccessVideo(Long userId, Long videoId) {
        Session session = sessionFactory.getCurrentSession();

        return Boolean.TRUE.equals(
                session.createQuery("""
                        select count(c.id) > 0
                        from VideoLessonEntity vl
                        join vl.lessonBlock lb
                        join lb.lesson l
                        join l.module m
                        join m.courseVersion cv
                        join CourseEntity c
                          on c.publishedVersion.id = cv.id
                          or c.draftVersion.id = cv.id
                        where vl.videoAssetId = :videoId
                          and c.authorId = :userId
                        """, Boolean.class)
                        .setParameter("videoId", videoId)
                        .setParameter("userId", userId)
                        .uniqueResult()
        );
    }

    @Override
    public boolean isPractice(Long courseLessonId) {
        Session session = sessionFactory.getCurrentSession();

        return Boolean.TRUE.equals(
                session.createQuery("""
                        select count(cl.id) > 0
                        from CourseLessonEntity cl
                        where cl.id = :courseLessonId
                          and cl.type = :practiceType
                        """, Boolean.class)
                        .setParameter("courseLessonId", courseLessonId)
                        .setParameter("practiceType", LessonType.PRACTICE)
                        .uniqueResult()
        );
    }

    @Override
    public Set<Long> findCourseIdsByFileId(Long fileId) {
        Session session = sessionFactory.getCurrentSession();

        return new HashSet<>(
                session.createQuery("""
                        select distinct c.id
                        from FileLessonEntity fl
                        join fl.lessonBlock lb
                        join lb.lesson l
                        join l.module m
                        join m.courseVersion cv
                        join CourseEntity c
                            on c.publishedVersion.id = cv.id
                            or c.draftVersion.id = cv.id
                        where fl.fileAssetId = :fileId
                        """, Long.class)
                        .setParameter("fileId", fileId)
                        .list()
        );
    }

    @Override
    public Set<Long> findCourseIdsByVideoId(Long videoId) {
        Session session = sessionFactory.getCurrentSession();

        return new HashSet<>(
                session.createQuery("""
                        select distinct c.id
                        from VideoLessonEntity vl
                        join vl.lessonBlock lb
                        join lb.lesson l
                        join l.module m
                        join m.courseVersion cv
                        join CourseEntity c
                            on c.publishedVersion.id = cv.id
                            or c.draftVersion.id = cv.id
                        where vl.videoAssetId = :videoId
                        """, Long.class)
                        .setParameter("videoId", videoId)
                        .list()
        );
    }

    private Course findByIdWithVersionContent(Long id, boolean draft) {
        Session session = sessionFactory.getCurrentSession();

        String versionField = draft
                ? "draftVersion"
                : "publishedVersion";

        CourseEntity entity = session.createQuery("""
                select distinct c
                from CourseEntity c
                left join fetch c.topicEntities
                left join fetch c.lessonOpeningEntities
                left join fetch c.%s v
                left join fetch v.modules m
                left join fetch m.lessons l
                left join fetch l.blocks b
                where c.id = :id
                """.formatted(versionField), CourseEntity.class)
                .setParameter("id", id)
                .uniqueResult();

        return courseMapper.toDomain(entity);
    }
}