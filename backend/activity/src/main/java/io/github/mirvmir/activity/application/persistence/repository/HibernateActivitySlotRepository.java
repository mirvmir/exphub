package io.github.mirvmir.activity.application.persistence.repository;

import io.github.mirvmir.activity.application.persistence.entity.ActivityEntity;
import io.github.mirvmir.activity.application.persistence.entity.ActivitySlotEntity;
import io.github.mirvmir.activity.application.persistence.mapper.ActivitySlotMapper;
import io.github.mirvmir.activity.application.service.port.repository.ActivitySlotRepository;
import io.github.mirvmir.activity.domain.Activity;
import io.github.mirvmir.activity.domain.ActivitySlot;
import io.github.mirvmir.activity.domain.ActivitySlotStatus;
import io.github.mirvmir.activity.exception.ActivityErrorCode;
import io.github.mirvmir.common.exception.BusinessException;
import jakarta.persistence.LockModeType;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Repository
public class HibernateActivitySlotRepository implements ActivitySlotRepository {

    private final SessionFactory sessionFactory;
    private final ActivitySlotMapper activitySlotMapper;

    @Override
    public ActivitySlot saveOrUpdate(ActivitySlot slot) {
        Session session = sessionFactory.getCurrentSession();

        ActivitySlotEntity entity = activitySlotMapper.toEntity(slot);

        if (entity.getId() == null) {
            session.persist(entity);
            session.flush();

            slot.assignId(entity.getId());

            return activitySlotMapper.toDomain(entity);
        }

        ActivitySlotEntity mergedEntity = session.merge(entity);

        return activitySlotMapper.toDomain(mergedEntity);
    }

    @Override
    public List<ActivitySlot> findPlannedByActivityId(Long activityId) {
        Session session = sessionFactory.getCurrentSession();

        return session.createQuery("""
                select s
                from ActivitySlotEntity s
                where s.activityEntity.id = :activityId
                  and s.status = :status
                order by s.startAt asc
                """, ActivitySlotEntity.class)
                .setParameter("activityId", activityId)
                .setParameter("status", ActivitySlotStatus.PLANNED)
                .getResultList()
                .stream()
                .map(activitySlotMapper::toDomain)
                .toList();
    }

    @Override
    public List<ActivitySlot> findByActivityId(Long activityId) {
        Session session = sessionFactory.getCurrentSession();

        return session.createQuery("""
                select s
                from ActivitySlotEntity s
                where s.activityEntity.id = :activityId
                order by s.startAt asc
                """, ActivitySlotEntity.class)
                .setParameter("activityId", activityId)
                .getResultList()
                .stream()
                .map(activitySlotMapper::toDomain)
                .toList();
    }

    @Override
    public ActivitySlot findById(Long activitySlotId) {
        Session session = sessionFactory.getCurrentSession();

        ActivitySlotEntity entity = session.find(
                ActivitySlotEntity.class,
                activitySlotId
        );

        return entity == null
                ? null
                : activitySlotMapper.toDomain(entity);
    }

    @Override
    public List<ActivitySlot> findByIds(Set<Long> activitySlotIds) {
        if (activitySlotIds == null || activitySlotIds.isEmpty()) {
            return List.of();
        }

        Session session = sessionFactory.getCurrentSession();

        return session.createQuery("""
                select s
                from ActivitySlotEntity s
                where s.id in (:activitySlotIds)
                order by s.startAt asc
                """, ActivitySlotEntity.class)
                .setParameter("activitySlotIds", activitySlotIds)
                .getResultList()
                .stream()
                .map(activitySlotMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsPlannedByActivityId(Long activityId) {
        Session session = sessionFactory.getCurrentSession();

        Long count = session.createQuery("""
                select count(s.id)
                from ActivitySlotEntity s
                where s.activityEntity.id = :activityId
                 and s.status = :status
                """, Long.class)
                .setParameter("activityId", activityId)
                .setParameter("status", ActivitySlotStatus.PLANNED)
                .uniqueResult();

        return count != null && count > 0;
    }

    @Override
    public ActivitySlot saveGroupSlotWithAuthorLock(Long authorId,
                                                    ActivitySlot slot) {
        Session session = sessionFactory.getCurrentSession();

        lockActivityForSlotCreation(
                session,
                slot.getActivityId(),
                authorId
        );

        boolean hasOverlap = existsPlannedActivitySlotForAuthor(
                authorId,
                slot.getStartAt(),
                slot.getEndAt()
        );

        if (hasOverlap) {
            throw new BusinessException(
                    ActivityErrorCode.ACTIVITY_SLOT_TIME_CONFLICT
            );
        }

        return saveOrUpdate(slot);
    }

    @Override
    public ActivitySlot saveIndividualSlotWithAuthorLock(Long authorId,
                                                         Long activityTimeId,
                                                         ActivitySlot slot) {
        Session session = sessionFactory.getCurrentSession();

        lockActivityForSlotCreation(
                session,
                slot.getActivityId(),
                authorId
        );

        boolean fitsActivityTime = existsActivityTimeContainingSlot(
                authorId,
                activityTimeId,
                slot.getStartAt(),
                slot.getEndAt()
        );

        if (!fitsActivityTime) {
            throw new BusinessException(
                    ActivityErrorCode.ACTIVITY_SLOT_TIME_CONFLICT
            );
        }

        boolean hasOverlap = existsPlannedActivitySlotForAuthor(
                authorId,
                slot.getStartAt(),
                slot.getEndAt()
        );

        if (hasOverlap) {
            throw new BusinessException(
                    ActivityErrorCode.ACTIVITY_TIME_ALREADY_BOOKED
            );
        }

        return saveOrUpdate(slot);
    }

    private void lockActivityForSlotCreation(
            Session session,
            Long activityId,
            Long authorId
    ) {
        ActivityEntity activityEntity = session.createQuery("""
                select a
                from ActivityEntity a
                where a.id = :activityId
                  and a.authorId = :authorId
                """, ActivityEntity.class)
                .setParameter("activityId", activityId)
                .setParameter("authorId", authorId)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .uniqueResult();

        if (activityEntity == null) {
            throw new BusinessException(
                    ActivityErrorCode.ACTIVITY_NOT_FOUND
            );
        }
    }

    private boolean existsPlannedActivitySlotForAuthor(Long authorId,
                                                       Instant startAt,
                                                       Instant endAt) {
        Session session = sessionFactory.getCurrentSession();

        Long count = session.createQuery("""
                select count(s.id)
                from ActivitySlotEntity s
                join s.activityEntity a
                where a.authorId = :authorId
                  and s.status = :status
                  and s.startAt < :endAt
                  and s.endAt > :startAt
                """, Long.class)
                .setParameter("authorId", authorId)
                .setParameter("status", ActivitySlotStatus.PLANNED)
                .setParameter("startAt", startAt)
                .setParameter("endAt", endAt)
                .uniqueResult();

        return count != null && count > 0;
    }

    private boolean existsActivityTimeContainingSlot(Long authorId,
                                                     Long activityTimeId,
                                                     Instant startAt,
                                                     Instant endAt) {
        Session session = sessionFactory.getCurrentSession();

        Long count = session.createQuery("""
                select count(t.id)
                from ActivityEntity a
                join a.activityTimeEntities t
                where a.authorId = :authorId
                  and t.id = :activityTimeId
                  and t.startAt <= :startAt
                  and t.endAt >= :endAt
                """, Long.class)
                .setParameter("authorId", authorId)
                .setParameter("activityTimeId", activityTimeId)
                .setParameter("startAt", startAt)
                .setParameter("endAt", endAt)
                .uniqueResult();

        return count != null && count > 0;
    }
}