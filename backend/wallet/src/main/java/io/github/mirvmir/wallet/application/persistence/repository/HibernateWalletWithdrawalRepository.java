package io.github.mirvmir.wallet.application.persistence.repository;

import io.github.mirvmir.wallet.application.service.port.repository.WalletWithdrawalRepository;
import io.github.mirvmir.wallet.domain.WalletWithdrawal;
import io.github.mirvmir.wallet.application.persistence.entity.WalletWithdrawalEntity;
import io.github.mirvmir.wallet.application.persistence.mapper.WalletWithdrawalMapper;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class HibernateWalletWithdrawalRepository
        implements WalletWithdrawalRepository {

    private final SessionFactory sessionFactory;
    private final WalletWithdrawalMapper walletWithdrawalMapper;

    @Override
    public WalletWithdrawal save(WalletWithdrawal withdrawal) {
        Session session = sessionFactory.getCurrentSession();

        WalletWithdrawalEntity entity =
                walletWithdrawalMapper.toEntity(withdrawal);

        if (entity.getId() == null) {
            session.persist(entity);
        } else {
            entity = session.merge(entity);
        }

        return walletWithdrawalMapper.toDomain(entity);
    }

    @Override
    public WalletWithdrawal findById(Long id) {
        Session session = sessionFactory.getCurrentSession();

        WalletWithdrawalEntity entity = session.get(
                WalletWithdrawalEntity.class,
                id
        );

        return walletWithdrawalMapper.toDomain(entity);
    }
}

