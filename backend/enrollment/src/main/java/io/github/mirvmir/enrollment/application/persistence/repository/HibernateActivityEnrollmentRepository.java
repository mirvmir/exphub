package io.github.mirvmir.enrollment.application.persistence.repository;

import io.github.mirvmir.enrollment.application.service.port.repository.ActivityEnrollmentRepository;
import io.github.mirvmir.enrollment.domain.ActivityEnrollment;
import io.github.mirvmir.enrollment.domain.ActivityEnrollmentStatus;
import io.github.mirvmir.enrollment.application.persistence.entity.ActivityEnrollmentEntity;
import io.github.mirvmir.enrollment.application.persistence.mapper.ActivityEnrollmentMapper;
import io.github.mirvmir.enrollment.application.properties.BookingProperties;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Repository
public class HibernateActivityEnrollmentRepository implements ActivityEnrollmentRepository {

    private final SessionFactory sessionFactory;
    private final ActivityEnrollmentMapper activityEnrollmentMapper;
    private final BookingProperties bookingProperties;

    @Override
    public ActivityEnrollment findById(Long id) {
        Session session = sessionFactory.getCurrentSession();

        ActivityEnrollmentEntity entity = session.find(
                ActivityEnrollmentEntity.class,
                id
        );

        if (entity == null) {
            return null;
        }

        return activityEnrollmentMapper.toDomain(entity);
    }

    @Override
    public ActivityEnrollment saveOrUpdate(ActivityEnrollment enrollment) {
        Session session = sessionFactory.getCurrentSession();

        ActivityEnrollmentEntity entity =
                activityEnrollmentMapper.toEntity(enrollment);

        if (entity.getId() == null) {
            session.persist(entity);
            session.flush();

            enrollment.assignId(entity.getId());

            return activityEnrollmentMapper.toDomain(entity);
        }

        ActivityEnrollmentEntity mergedEntity =
                session.merge(entity);

        return activityEnrollmentMapper.toDomain(mergedEntity);
    }

    @Override
    public boolean existsActiveByUserIdAndActivitySlotId(Long userId,
                                                         Long activitySlotId,
                                                         Instant now) {
        Session session = sessionFactory.getCurrentSession();

        Boolean exists = session.createQuery("""
                select count(e.id) > 0
                from ActivityEnrollmentEntity e
                where e.userId = :userId
                  and e.activitySlotId = :activitySlotId
                  and (
                        e.status = :payedStatus
                        or (
                            e.status = :bookedStatus
                            and e.subscribedAt > :activeBookedAfter
                        )
                  )
                """, Boolean.class)
                .setParameter("userId", userId)
                .setParameter("activitySlotId", activitySlotId)
                .setParameter("payedStatus", ActivityEnrollmentStatus.PAYED)
                .setParameter("bookedStatus", ActivityEnrollmentStatus.BOOKED)
                .setParameter("activeBookedAfter", now.minus(
                        bookingProperties.getBookingExpiresMinutes(),
                        ChronoUnit.MINUTES
                ))
                .uniqueResult();

        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean existsPayedByUserIdAndActivityId(Long userId,
                                                    Long activityId) {
        Session session = sessionFactory.getCurrentSession();

        Boolean exists = session.createQuery("""
                select count(e.id) > 0
                from ActivityEnrollmentEntity e
                where e.userId = :userId
                  and e.activityId = :activityId
                  and e.status = :status
                """, Boolean.class)
                .setParameter("userId", userId)
                .setParameter("activityId", activityId)
                .setParameter("status", ActivityEnrollmentStatus.PAYED)
                .uniqueResult();

        return Boolean.TRUE.equals(exists);
    }

    @Override
    public Map<Long, Integer> countBookedByActivitySlotIds(Set<Long> activitySlotIds,
                                                           Instant now) {
        if (activitySlotIds == null || activitySlotIds.isEmpty()) {
            return Map.of();
        }

        Session session = sessionFactory.getCurrentSession();

        Instant activeBookedAfter = now.minus(
                bookingProperties.getBookingExpiresMinutes(),
                ChronoUnit.MINUTES
        );

        List<Object[]> rows = session.createQuery("""
                select e.activitySlotId, count(e.id)
                from ActivityEnrollmentEntity e
                where e.activitySlotId in :activitySlotIds
                  and (
                        e.status = :payedStatus
                        or (
                            e.status = :bookedStatus
                            and e.subscribedAt > :activeBookedAfter
                        )
                  )
                group by e.activitySlotId
                """, Object[].class)
                .setParameter("activitySlotIds", activitySlotIds)
                .setParameter(
                        "payedStatus",
                        ActivityEnrollmentStatus.PAYED
                )
                .setParameter(
                        "bookedStatus",
                        ActivityEnrollmentStatus.BOOKED
                )
                .setParameter(
                        "activeBookedAfter",
                        activeBookedAfter
                )
                .getResultList();

        return rows.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()
                ));
    }

    @Override
    public ActivityEnrollment tryEnroll(Long activityId,
                                        Long activitySlotId,
                                        Long userId,
                                        Instant now) {
        Session session = sessionFactory.getCurrentSession();

        Instant activeBookedAfter = now.minus(
                bookingProperties.getBookingExpiresMinutes(),
                ChronoUnit.MINUTES
        );

        Number result = (Number) session.createNativeQuery("""
                insert into activity_enrollment (
                    activity_id,
                    activity_slot_id,
                    user_id,
                    status,
                    subscribed_at
                )
                select
                    s.activity_id,
                    s.id,
                    :userId,
                    :status,
                    :subscribedAt
                from activity_slot s
                where s.id = :activitySlotId
                  and s.activity_id = :activityId
                  and (
                      select count(ae.id)
                      from activity_enrollment ae
                      where ae.activity_slot_id = s.id
                        and (
                            ae.status = :payedStatus
                            or (
                                ae.status = :bookedStatus
                                and ae.subscribed_at > :activeBookedAfter
                            )
                        )
                  ) < s.capacity
                  and not exists (
                      select 1
                      from activity_enrollment ae
                      where ae.activity_slot_id = s.id
                        and ae.user_id = :userId
                        and (
                            ae.status = :payedStatus
                            or (
                                ae.status = :bookedStatus
                                and ae.subscribed_at > :activeBookedAfter
                            )
                        )
                  )
                for update of s
                returning id
                """)
                .setParameter("activitySlotId", activitySlotId)
                .setParameter("activityId", activityId)
                .setParameter("userId", userId)
                .setParameter("status", ActivityEnrollmentStatus.BOOKED.name())
                .setParameter("subscribedAt", now)
                .setParameter("payedStatus", ActivityEnrollmentStatus.PAYED.name())
                .setParameter("bookedStatus", ActivityEnrollmentStatus.BOOKED.name())
                .setParameter("activeBookedAfter", activeBookedAfter)
                .uniqueResult();

        if (result == null) {
            return null;
        }

        Long enrollmentId = result.longValue();

        return ActivityEnrollment.load(
                enrollmentId,
                activityId,
                activitySlotId,
                userId,
                ActivityEnrollmentStatus.BOOKED,
                now
        );
    }

    @Override
    public List<ActivityEnrollment> findActiveByStudentId(Long studentId) {
        return sessionFactory.getCurrentSession()
                .createQuery("""
                select e
                from ActivityEnrollmentEntity e
                where e.userId = :userId
                """, ActivityEnrollmentEntity.class)
                .setParameter("userId", studentId)
                .list()
                .stream()
                .map(activityEnrollmentMapper::toDomain)
                .toList();
    }

    @Override
    public List<ActivityEnrollment> findActiveByActivitySlotId(Long activitySlotId) {
        return sessionFactory.getCurrentSession()
                .createQuery("""
                select e
                from ActivityEnrollmentEntity e
                where e.activitySlotId = :activitySlotId
                  and e.status in (:statuses)
                """, ActivityEnrollmentEntity.class)
                .setParameter("activitySlotId", activitySlotId)
                .setParameterList("statuses", List.of(
                        ActivityEnrollmentStatus.BOOKED,
                        ActivityEnrollmentStatus.PAYED
                ))
                .list()
                .stream()
                .map(activityEnrollmentMapper::toDomain)
                .toList();
    }

    @Override
    public List<ActivityEnrollment> findPayedByActivitySlotId(Long activitySlotId) {
        return sessionFactory.getCurrentSession()
                .createQuery("""
                select e
                from ActivityEnrollmentEntity e
                where e.activitySlotId = :activitySlotId
                  and e.status = :status
                """, ActivityEnrollmentEntity.class)
                .setParameter("activitySlotId", activitySlotId)
                .setParameter("status", ActivityEnrollmentStatus.PAYED)
                .list()
                .stream()
                .map(activityEnrollmentMapper::toDomain)
                .toList();
    }

    @Override
    public ActivityEnrollment findActiveByActivitySlotIdAndStudentId(Long activitySlotId,
                                                                     Long studentId) {
        ActivityEnrollmentEntity entity = sessionFactory.getCurrentSession()
                .createQuery("""
                select e
                from ActivityEnrollmentEntity e
                where e.activitySlotId = :activitySlotId
                  and e.userId = :studentId
                  and e.status in (:statuses)
                """, ActivityEnrollmentEntity.class)
                .setParameter("activitySlotId", activitySlotId)
                .setParameter("studentId", studentId)
                .setParameterList("statuses", List.of(
                        ActivityEnrollmentStatus.BOOKED,
                        ActivityEnrollmentStatus.PAYED
                ))
                .uniqueResult();

        if (entity == null) {
            return null;
        }

        return activityEnrollmentMapper.toDomain(entity);
    }
}
