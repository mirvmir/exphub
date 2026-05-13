package io.github.mirvmir.payment.application.persistence.repository;

import io.github.mirvmir.payment.application.service.port.repository.PayoutRepository;
import io.github.mirvmir.payment.domain.Payout;
import io.github.mirvmir.payment.application.persistence.entity.PayoutEntity;
import io.github.mirvmir.payment.application.persistence.mapper.PayoutMapper;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class HibernatePayoutRepository implements PayoutRepository {

    private final SessionFactory sessionFactory;
    private final PayoutMapper payoutMapper;

    @Override
    public Payout saveOrUpdate(Payout payout) {
        Session session = sessionFactory.getCurrentSession();

        PayoutEntity entity = payoutMapper.toEntity(payout);

        if (entity.getId() == null) {
            session.persist(entity);
        } else {
            entity = session.merge(entity);
        }

        return payoutMapper.toDomain(entity);
    }

    @Override
    public Payout findById(Long id) {
        Session session = sessionFactory.getCurrentSession();

        PayoutEntity entity = session.get(PayoutEntity.class, id);

        return payoutMapper.toDomain(entity);
    }

    @Override
    public Payout findByExternalPayoutId(String externalPayoutId) {
        Session session = sessionFactory.getCurrentSession();

        PayoutEntity entity = session.createQuery("""
                from PayoutEntity p
                where p.externalPayoutId = :externalPayoutId
                """, PayoutEntity.class)
                .setParameter("externalPayoutId", externalPayoutId)
                .uniqueResult();

        return payoutMapper.toDomain(entity);
    }

    @Override
    public boolean existsByWalletWithdrawalId(Long walletWithdrawalId) {
        Session session = sessionFactory.getCurrentSession();

        return Boolean.TRUE.equals(
                session.createQuery("""
                        select count(p.id) > 0
                        from PayoutEntity p
                        where p.walletWithdrawalId = :walletWithdrawalId
                        """, Boolean.class)
                        .setParameter("walletWithdrawalId", walletWithdrawalId)
                        .uniqueResult()
        );
    }
}