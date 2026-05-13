package io.github.mirvmir.payment.application.persistence.repository;

import io.github.mirvmir.payment.application.persistence.entity.RefundEntity;
import io.github.mirvmir.payment.application.persistence.mapper.RefundMapper;
import io.github.mirvmir.payment.application.service.port.repository.RefundRepository;
import io.github.mirvmir.payment.domain.Refund;
import io.github.mirvmir.payment.domain.RefundStatus;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
@RequiredArgsConstructor
public class HibernateRefundRepository implements RefundRepository {

    private final SessionFactory sessionFactory;

    private final RefundMapper refundMapper;

    @Override
    public Refund saveOrUpdate(Refund refund) {
        Session session = sessionFactory.getCurrentSession();

        RefundEntity entity = refundMapper.toEntity(refund);

        if (entity.getId() == null) {
            session.persist(entity);
        } else {
            entity = session.merge(entity);
        }

        return refundMapper.toDomain(entity);
    }

    @Override
    public Refund findById(Long id) {
        Session session = sessionFactory.getCurrentSession();

        RefundEntity entity = session.find(RefundEntity.class, id);

        if (entity == null) {
            return null;
        }

        return refundMapper.toDomain(entity);
    }

    @Override
    public Refund findByExternalRefundId(String externalRefundId) {
        Session session = sessionFactory.getCurrentSession();

        RefundEntity entity = session.createQuery("""
                select r
                from RefundEntity r
                where r.externalRefundId = :externalRefundId
                """, RefundEntity.class)
                .setParameter("externalRefundId", externalRefundId)
                .uniqueResult();

        if (entity == null) {
            return null;
        }

        return refundMapper.toDomain(entity);
    }

    @Override
    public boolean existsByPaymentIdAndStatusIn(Long externalRefundId,
                                                Collection<RefundStatus> statuses) {
        Session session = sessionFactory.getCurrentSession();

        return Boolean.TRUE.equals(
                session.createQuery("""
                        select count(r.id) > 0
                        from RefundEntity r
                        where r.externalRefundId = :externalRefundId
                          and r.status in (:statuses)
                        """, Boolean.class)
                        .setParameter("externalRefundId", externalRefundId)
                        .setParameterList("statuses", statuses)
                        .uniqueResult()
        );
    }
}