package io.github.mirvmir.activity.application.persistence.repository;

import io.github.mirvmir.activity.application.service.port.repository.ActivityRepository;
import io.github.mirvmir.activity.domain.Activity;
import io.github.mirvmir.activity.application.persistence.entity.ActivityEntity;
import io.github.mirvmir.activity.application.persistence.mapper.ActivityMapper;
import io.github.mirvmir.common.domain.ContentStatus;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@AllArgsConstructor
@Repository
public class HibernateActivityRepository implements ActivityRepository {

    private final SessionFactory sessionFactory;
    private final ActivityMapper activityMapper;

    @Override
    public Activity saveOrUpdate(Activity activity) {
        Session session = sessionFactory.getCurrentSession();

        ActivityEntity entity = activityMapper.toEntity(activity);

        if (entity.getId() == null) {
            session.persist(entity);
            session.flush();
            activity.assignId(entity.getId());
        } else {
            entity = session.merge(entity);
        }

        return activityMapper.toDomain(entity);
    }

    @Override
    public Activity findById(Long id) {
        Session session = sessionFactory.getCurrentSession();

        ActivityEntity entity = session.createQuery("""
                select distinct a
                from ActivityEntity a
                left join fetch a.topicEntities
                left join fetch a.activityTimeEntities
                where a.id = :id
                """, ActivityEntity.class)
                .setParameter("id", id)
                .uniqueResult();

        return activityMapper.toDomain(entity);
    }

    @Override
    public List<Activity> findByAuthorId(Long authorId) {
        Session session = sessionFactory.getCurrentSession();

        List<ActivityEntity> entities = session.createQuery("""
                select distinct a
                from ActivityEntity a
                left join fetch a.topicEntities
                where a.authorId = :authorId
                  and a.contentStatus <> :deletedStatus
                order by a.id desc
                """, ActivityEntity.class)
                .setParameter("authorId", authorId)
                .setParameter("deletedStatus", ContentStatus.DELETED)
                .getResultList();

        return entities.stream()
                .map(activityMapper::toDomain)
                .toList();
    }

    @Override
    public Activity findActiveById(Long id) {
        Session session = sessionFactory.getCurrentSession();

        ActivityEntity entity = session.createQuery("""
                select distinct a
                from ActivityEntity a
                left join fetch a.topicEntities
                left join fetch a.activityTimeEntities
                where a.id = :id
                  and a.contentStatus = :contentStatus
                """, ActivityEntity.class)
                .setParameter("id", id)
                .setParameter("contentStatus", ContentStatus.ACTIVE)
                .uniqueResult();

        return activityMapper.toDomain(entity);
    }
}