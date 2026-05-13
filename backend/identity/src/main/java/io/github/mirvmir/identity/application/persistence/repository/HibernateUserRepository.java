package io.github.mirvmir.identity.application.persistence.repository;

import io.github.mirvmir.identity.domain.User;
import io.github.mirvmir.identity.application.service.port.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class HibernateUserRepository implements UserRepository {

    private final SessionFactory sessionFactory;

    @Override
    public User save(User user) {
        Session session = sessionFactory.getCurrentSession();
        session.persist(user);
        session.flush();
        return user;
    }

    @Override
    public boolean existUser(Long userId) {
        Session session = sessionFactory.getCurrentSession();
        return session.get(User.class, userId) != null;
    }

    @Override
    public User findById(Long userId) {
        Session session = sessionFactory.getCurrentSession();
        return session.get(User.class, userId);
    }

    @Override
    public User findByEmail(String email) {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery("FROM User WHERE email = :email", User.class)
                .setParameter("email", email)
                .uniqueResult();
    }
}
