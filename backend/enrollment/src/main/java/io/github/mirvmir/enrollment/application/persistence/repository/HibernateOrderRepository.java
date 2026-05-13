package io.github.mirvmir.enrollment.application.persistence.repository;

import io.github.mirvmir.enrollment.application.service.port.repository.OrderRepository;
import io.github.mirvmir.enrollment.domain.order.Order;
import io.github.mirvmir.enrollment.domain.order.OrderStatus;
import io.github.mirvmir.enrollment.domain.order.OrderTargetType;
import io.github.mirvmir.enrollment.application.persistence.entity.OrderEntity;
import io.github.mirvmir.enrollment.application.persistence.mapper.OrderMapper;
import jakarta.persistence.LockModeType;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@AllArgsConstructor
public class HibernateOrderRepository implements OrderRepository {

    private final SessionFactory sessionFactory;

    private final OrderMapper orderMapper;

    @Override
    public Order saveOrUpdate(Order order) {
        Session session = sessionFactory.getCurrentSession();

        OrderEntity entity = orderMapper.toEntity(order);

        if (entity.getId() == null) {
            session.persist(entity);
            return orderMapper.toDomain(entity);
        }

        OrderEntity merged = session.merge(entity);
        return orderMapper.toDomain(merged);
    }

    @Override
    public Order findById(Long id) {
        Session session = sessionFactory.getCurrentSession();

        OrderEntity entity = session.find(
                OrderEntity.class,
                id
        );

        if (entity == null) {
            return null;
        }

        return orderMapper.toDomain(entity);
    }

    @Override
    public Order findByIdForUpdate(Long id) {
        Session session = sessionFactory.getCurrentSession();

        OrderEntity entity = session.find(
                OrderEntity.class,
                id,
                LockModeType.PESSIMISTIC_WRITE
        );

        if (entity == null) {
            return null;
        }

        return orderMapper.toDomain(entity);
    }

    @Override
    public List<Order> findExpiredForUpdate(Instant now,
                                            int limit) {
        Session session = sessionFactory.getCurrentSession();

        return session.createQuery("""
                select o
                from OrderEntity o
                where o.expiresAt <= :now
                  and o.status in (:statuses)
                order by o.expiresAt asc
                """, OrderEntity.class)
                .setParameter("now", now)
                .setParameterList("statuses", List.of(
                        OrderStatus.CREATED,
                        OrderStatus.PAYMENT_PROCESSING
                ))
                .setMaxResults(limit)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList()
                .stream()
                .map(orderMapper::toDomain)
                .toList();
    }

    @Override
    public Order findByEnrollmentId(Long enrollmentId, OrderTargetType type) {
        Session session = sessionFactory.getCurrentSession();

        OrderEntity entity = session.createQuery("""
                select o
                from OrderEntity o
                where o.enrollmentId = :enrollmentId
                  and o.targetType = :targetType
                order by o.createdAt desc
                """, OrderEntity.class)
                .setParameter("enrollmentId", enrollmentId)
                .setParameter("targetType", type)
                .setMaxResults(1)
                .uniqueResult();

        if (entity == null) {
            return null;
        }

        return orderMapper.toDomain(entity);
    }

    @Override
    public Order findByEnrollmentIdForUpdate(Long enrollmentId,
                                             OrderTargetType type) {
        Session session = sessionFactory.getCurrentSession();

        OrderEntity entity = session.createQuery("""
                select o
                from OrderEntity o
                where o.enrollmentId = :enrollmentId
                  and o.targetType = :targetType
                order by o.createdAt desc
                """, OrderEntity.class)
                .setParameter("enrollmentId", enrollmentId)
                .setParameter("targetType", type)
                .setMaxResults(1)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .uniqueResult();

        if (entity == null) {
            return null;
        }

        return orderMapper.toDomain(entity);
    }
}
