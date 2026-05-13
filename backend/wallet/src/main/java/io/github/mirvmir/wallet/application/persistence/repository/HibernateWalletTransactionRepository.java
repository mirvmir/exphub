package io.github.mirvmir.wallet.application.persistence.repository;

import io.github.mirvmir.wallet.application.service.port.repository.WalletTransactionRepository;
import io.github.mirvmir.wallet.domain.WalletTransaction;
import io.github.mirvmir.wallet.domain.WalletTransactionType;
import io.github.mirvmir.wallet.application.persistence.entity.WalletTransactionEntity;
import io.github.mirvmir.wallet.application.persistence.mapper.WalletTransactionMapper;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class HibernateWalletTransactionRepository implements WalletTransactionRepository {

    private final SessionFactory sessionFactory;
    private final WalletTransactionMapper mapper;

    @Override
    public boolean existsByPaymentId(Long paymentId, WalletTransactionType type) {
        Session session = sessionFactory.getCurrentSession();

        return session.createQuery("""
                select count(t.id) > 0
                from WalletTransactionEntity t
                where t.paymentId = :paymentId
                  and t.type = :type
                """, Boolean.class)
                .setParameter("paymentId", paymentId)
                .setParameter("type", type)
                .uniqueResult();
    }

    @Override
    public boolean existsByWalletWithdrawalId(Long walletWithdrawalId, WalletTransactionType type) {
        Session session = sessionFactory.getCurrentSession();

        return session.createQuery("""
                select count(t.id) > 0
                from WalletTransactionEntity t
                where t.walletWithdrawalId = :walletWithdrawalId
                  and t.type = :type
                """, Boolean.class)
                .setParameter("walletWithdrawalId", walletWithdrawalId)
                .setParameter("type", type)
                .uniqueResult();
    }

    @Override
    public WalletTransaction save(WalletTransaction transaction) {
        Session session = sessionFactory.getCurrentSession();

        WalletTransactionEntity entity = mapper.toEntity(transaction);

        if (entity.getId() == null) {
            session.persist(entity);
        } else {
            entity = session.merge(entity);
        }

        return mapper.toDomain(entity);
    }
}