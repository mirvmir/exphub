package io.github.mirvmir.payment.application.persistence.repository;

import io.github.mirvmir.payment.application.service.port.repository.UserCardRepository;
import io.github.mirvmir.payment.domain.UserCard;
import io.github.mirvmir.payment.application.persistence.entity.UserCardEntity;
import io.github.mirvmir.payment.application.persistence.mapper.UserCardMapper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class HibernateUserCardRepository implements UserCardRepository {

    private final SessionFactory sessionFactory;
    private final UserCardMapper userCardMapper;

    public HibernateUserCardRepository(SessionFactory sessionFactory,
                                       UserCardMapper userCardMapper) {
        this.sessionFactory = sessionFactory;
        this.userCardMapper = userCardMapper;
    }

    @Override
    public UserCard save(UserCard card) {
        Session session = sessionFactory.getCurrentSession();

        UserCardEntity entity = userCardMapper.toEntity(card);

        if (entity.getId() == null) {
            session.persist(entity);
        } else {
            entity = session.merge(entity);
        }

        return userCardMapper.toDomain(entity);
    }

    @Override
    public UserCard findById(Long id) {
        Session session = sessionFactory.getCurrentSession();

        UserCardEntity entity = session.find(UserCardEntity.class, id);

        return userCardMapper.toDomain(entity);
    }

    @Override
    public List<UserCard> findByUserId(Long userId) {
        Session session = sessionFactory.getCurrentSession();

        return session.createQuery("""
                select c
                from UserCardEntity c
                where c.userId = :userId
                  and c.active = true
                order by c.createdAt desc
                """, UserCardEntity.class)
                .setParameter("userId", userId)
                .getResultList()
                .stream()
                .map(userCardMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByUserIdAndCardToken(Long userId, String cardToken) {
        Session session = sessionFactory.getCurrentSession();

        return Boolean.TRUE.equals(
                session.createQuery("""
                        select count(c.id) > 0
                        from UserCardEntity c
                        where c.userId = :userId
                          and c.cardToken = :cardToken
                        """, Boolean.class)
                        .setParameter("userId", userId)
                        .setParameter("cardToken", cardToken)
                        .uniqueResult()
        );
    }

    @Override
    public UserCard findDefaultByUserId(Long userId) {
        Session session = sessionFactory.getCurrentSession();

        UserCardEntity entity = session.createQuery("""
                select c
                from UserCardEntity c
                where c.userId = :userId
                  and c.active = true
                  and c.defaultCard = true
                """, UserCardEntity.class)
                .setParameter("userId", userId)
                .uniqueResult();

        return userCardMapper.toDomain(entity);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        Session session = sessionFactory.getCurrentSession();

        return Boolean.TRUE.equals(
                session.createQuery("""
                        select count(c.id) > 0
                        from UserCardEntity c
                        where c.userId = :userId
                        """, Boolean.class)
                        .setParameter("userId", userId)
                        .uniqueResult()
        );
    }
}