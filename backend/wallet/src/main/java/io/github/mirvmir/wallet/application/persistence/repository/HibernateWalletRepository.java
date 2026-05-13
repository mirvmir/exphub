package io.github.mirvmir.wallet.application.persistence.repository;

import io.github.mirvmir.wallet.application.service.port.repository.WalletRepository;
import io.github.mirvmir.wallet.domain.Wallet;
import io.github.mirvmir.wallet.application.persistence.entity.WalletEntity;
import io.github.mirvmir.wallet.application.persistence.mapper.WalletMapper;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class HibernateWalletRepository implements WalletRepository {

    private final SessionFactory sessionFactory;

    private final WalletMapper walletMapper;

    @Override
    public Wallet findByUserId(Long teacherId) {
        Session session = sessionFactory.getCurrentSession();

        WalletEntity entity = session.createQuery("""
                from WalletEntity w
                where w.teacherId = :teacherId
                """, WalletEntity.class)
                .setParameter("teacherId", teacherId)
                .uniqueResult();

        return entity == null ? null : walletMapper.toDomain(entity);
    }

    @Override
    public Wallet saveOrUpdate(Wallet wallet) {
        Session session = sessionFactory.getCurrentSession();

        WalletEntity entity = walletMapper.toEntity(wallet);

        if (entity.getId() == null) {
            session.persist(entity);
        } else {
            entity = session.merge(entity);
        }

        return walletMapper.toDomain(entity);
    }
}
