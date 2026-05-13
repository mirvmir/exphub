package io.github.mirvmir.payment.application.persistence.repository;

import io.github.mirvmir.payment.application.service.port.repository.PaymentRepository;
import io.github.mirvmir.payment.domain.Payment;
import io.github.mirvmir.payment.application.persistence.entity.PaymentEntity;
import io.github.mirvmir.payment.application.persistence.mapper.PaymentMapper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class HibernatePaymentRepository implements PaymentRepository {

    private final SessionFactory sessionFactory;
    private final PaymentMapper paymentMapper;

    public HibernatePaymentRepository(SessionFactory sessionFactory,
                                      PaymentMapper paymentMapper) {
        this.sessionFactory = sessionFactory;
        this.paymentMapper = paymentMapper;
    }

    @Override
    public Payment save(Payment payment) {
        Session session = sessionFactory.getCurrentSession();

        PaymentEntity entity = paymentMapper.toEntity(payment);

        if (entity.getId() == null) {
            session.persist(entity);
        } else {
            entity = session.merge(entity);
        }

        return paymentMapper.toDomain(entity);
    }

    @Override
    public Payment findById(Long id) {
        Session session = sessionFactory.getCurrentSession();

        PaymentEntity entity = session.find(PaymentEntity.class, id);

        return paymentMapper.toDomain(entity);
    }

    @Override
    public Payment findByExternalPaymentId(String externalPaymentId) {
        Session session = sessionFactory.getCurrentSession();

        PaymentEntity entity = session.createQuery("""
                select p
                from PaymentEntity p
                where p.externalPaymentId = :externalPaymentId
                """, PaymentEntity.class)
                .setParameter("externalPaymentId", externalPaymentId)
                .uniqueResult();

        return paymentMapper.toDomain(entity);
    }

    @Override
    public List<Payment> findByUserId(Long userId) {
        Session session = sessionFactory.getCurrentSession();

        return session.createQuery("""
                select p
                from PaymentEntity p
                where p.userId = :userId
                order by p.createdAt desc
                """, PaymentEntity.class)
                .setParameter("userId", userId)
                .getResultList()
                .stream()
                .map(paymentMapper::toDomain)
                .toList();
    }

    @Override
    public Payment findByOrderId(Long orderId) {
        Session session = sessionFactory.getCurrentSession();

        PaymentEntity entity = session.createQuery("""
                select p
                from PaymentEntity p
                where p.orderId = :orderId
                """, PaymentEntity.class)
                .setParameter("orderId", orderId)
                .uniqueResult();

        return paymentMapper.toDomain(entity);
    }
}