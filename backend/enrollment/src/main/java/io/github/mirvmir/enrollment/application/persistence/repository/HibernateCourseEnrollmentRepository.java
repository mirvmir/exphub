package io.github.mirvmir.enrollment.application.persistence.repository;

import io.github.mirvmir.enrollment.application.properties.BookingProperties;
import io.github.mirvmir.enrollment.application.service.port.repository.CourseEnrollmentRepository;
import io.github.mirvmir.enrollment.domain.CourseEnrollment;
import io.github.mirvmir.enrollment.domain.CourseEnrollmentStatus;
import io.github.mirvmir.enrollment.application.persistence.entity.CourseEnrollmentEntity;
import io.github.mirvmir.enrollment.application.persistence.mapper.CourseEnrollmentMapper;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Repository
public class HibernateCourseEnrollmentRepository implements CourseEnrollmentRepository {

    private final SessionFactory sessionFactory;
    private final CourseEnrollmentMapper courseEnrollmentMapper;
    private final BookingProperties bookingProperties;

    @Override
    public CourseEnrollment findById(Long id) {
        Session session = sessionFactory.getCurrentSession();

        CourseEnrollmentEntity entity = session.find(
                CourseEnrollmentEntity.class,
                id
        );

        if (entity == null) {
            return null;
        }

        return courseEnrollmentMapper.toDomain(entity);
    }

    @Override
    public CourseEnrollment saveOrUpdate(CourseEnrollment enrollment) {
        Session session = sessionFactory.getCurrentSession();

        CourseEnrollmentEntity entity = courseEnrollmentMapper.toEntity(enrollment);

        if (entity.getId() == null) {
            session.persist(entity);
            return courseEnrollmentMapper.toDomain(entity);
        }

        CourseEnrollmentEntity merged = session.merge(entity);
        return courseEnrollmentMapper.toDomain(merged);
    }

    @Override
    public boolean existsPayedByUserIdAndCourseId(Long userId,
                                                  Long courseId) {
        Session session = sessionFactory.getCurrentSession();

        Boolean exists = session.createQuery("""
                select count(e.id) > 0
                from CourseEnrollmentEntity e
                where e.userId = :userId
                  and e.courseId = :courseId
                  and e.status = :status
                """, Boolean.class)
                .setParameter("userId", userId)
                .setParameter("courseId", courseId)
                .setParameter("status", CourseEnrollmentStatus.PAYED)
                .uniqueResult();

        return Boolean.TRUE.equals(exists);
    }


    @Override
    public boolean existsPayedByUserIdAndCourseIds(Long userId,
                                                   Set<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return false;
        }

        return Boolean.TRUE.equals(
                sessionFactory.getCurrentSession()
                        .createQuery("""
                        select count(e.id) > 0
                        from CourseEnrollmentEntity e
                        where e.userId = :userId
                          and e.courseId in (:courseIds)
                          and e.status = :status
                        """, Boolean.class)
                        .setParameter("userId", userId)
                        .setParameterList("courseIds", courseIds)
                        .setParameter("status", CourseEnrollmentStatus.PAYED)
                        .uniqueResult()
        );
    }

    @Override
    public boolean existsActiveByUserIdAndCourseId(Long userId,
                                                   Long courseId,
                                                   Instant now) {
        Instant activeBookedAfter = now.minus(
                bookingProperties.getBookingExpiresMinutes(),
                ChronoUnit.MINUTES
        );

        return Boolean.TRUE.equals(
                sessionFactory.getCurrentSession()
                        .createQuery("""
                        select count(e.id) > 0
                        from CourseEnrollmentEntity e
                        where e.userId = :userId
                          and e.courseId = :courseId
                          and (
                            e.status = :payedStatus
                            or (
                                e.status = :bookedStatus
                                and e.subscribedAt > :activeBookedAfter
                            )
                          )
                        """, Boolean.class)
                        .setParameter("userId", userId)
                        .setParameter("courseId", courseId)
                        .setParameter("payedStatus", CourseEnrollmentStatus.PAYED)
                        .setParameter("bookedStatus", CourseEnrollmentStatus.BOOKED)
                        .setParameter("activeBookedAfter", activeBookedAfter)
                        .uniqueResult()
        );
    }

    @Override
    public List<CourseEnrollment> findActiveByCourseId(Long courseId) {
        List<CourseEnrollmentEntity> entities = sessionFactory.getCurrentSession()
                .createQuery("""
                select e
                from CourseEnrollmentEntity e
                where e.courseId = :courseId
                  and e.status in (:courseStatuses)
                """, CourseEnrollmentEntity.class)
                .setParameter("courseId", courseId)
                .setParameterList("courseStatuses", List.of(
                        CourseEnrollmentStatus.BOOKED,
                        CourseEnrollmentStatus.PAYED
                ))
                .list();

        return entities.stream()
                .map(courseEnrollmentMapper::toDomain)
                .toList();
    }

    @Override
    public CourseEnrollment findPayedByUserIdAndCourseId(Long userId,
                                                         Long courseId) {
        CourseEnrollmentEntity entity = sessionFactory.getCurrentSession()
                .createQuery("""
                select e
                from CourseEnrollmentEntity e
                where e.userId = :userId
                  and e.courseId = :courseId
                  and e.status = (:status)
                """, CourseEnrollmentEntity.class)
                .setParameter("userId", userId)
                .setParameter("courseId", courseId)
                .setParameter("status", CourseEnrollmentStatus.PAYED)
                .uniqueResult();

        return courseEnrollmentMapper.toDomain(entity);
    }

    @Override
    public List<CourseEnrollment> findPayedByCourseId(Long courseId) {
        List<CourseEnrollmentEntity> entities = sessionFactory.getCurrentSession()
                .createQuery("""
                select e
                from CourseEnrollmentEntity e
                where e.courseId = :courseId
                  and e.status = :status
                """, CourseEnrollmentEntity.class)
                .setParameter("courseId", courseId)
                .setParameter("status", CourseEnrollmentStatus.PAYED)
                .list();

        return entities.stream()
                .map(courseEnrollmentMapper::toDomain)
                .toList();
    }
}
