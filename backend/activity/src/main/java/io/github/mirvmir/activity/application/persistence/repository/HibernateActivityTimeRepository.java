package io.github.mirvmir.activity.application.persistence.repository;

import io.github.mirvmir.activity.application.service.port.repository.ActivityTimeRepository;
import io.github.mirvmir.activity.exception.ActivityErrorCode;
import io.github.mirvmir.common.exception.NotFoundException;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class HibernateActivityTimeRepository implements ActivityTimeRepository {

    private final SessionFactory sessionFactory;

    @Override
    public void deleteByActivityIdAndId(Long activityId,
                                        Long activityTimeId) {
        Session session = sessionFactory.getCurrentSession();

        int deleted = session.createMutationQuery("""
                delete from ActivityTimeEntity t
                where t.id = :activityTimeId
                  and t.id in (
                        select at.id
                        from ActivityEntity a
                        join a.activityTimeEntities at
                        where a.id = :activityId
                  )
                """)
                .setParameter("activityId", activityId)
                .setParameter("activityTimeId", activityTimeId)
                .executeUpdate();

        if (0 == deleted) {
            throw new NotFoundException(ActivityErrorCode.TIME_NOT_FOUND);
        }
    }
}