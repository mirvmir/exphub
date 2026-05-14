package io.github.mirvmir.identity.application.persistence.repository;

import io.github.mirvmir.identity.application.service.port.repository.RefreshTokenRepository;
import io.github.mirvmir.identity.domain.RefreshToken;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@AllArgsConstructor
@Repository
public class HibernateRefreshTokenRepository implements RefreshTokenRepository {

    private final SessionFactory sessionFactory;

    @Override
    public RefreshToken findByTokenHash(String tokenHash) {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery(
                "FROM RefreshToken WHERE tokenHash = :tokenHash", RefreshToken.class
                )
                .setParameter("tokenHash", tokenHash)
                .uniqueResult();
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        Session session = sessionFactory.getCurrentSession();

        session.persist(refreshToken);
        return refreshToken;
    }

    @Override
    public void delete(RefreshToken refreshToken) {
        Session session = sessionFactory.getCurrentSession();
        session.remove(refreshToken);
    }

    @Override
    public int deleteExpiredBatch(Instant now, int batchSize) {
        Session session = sessionFactory.getCurrentSession();

        return session.createNativeQuery("""
            delete from refresh_token
            where ctid in (
                select ctid
                from refresh_token
                where expires_at < :now
                limit :batchSize
            )
            """)
                .setParameter("now", now)
                .setParameter("batchSize", batchSize)
                .executeUpdate();
    }
}

