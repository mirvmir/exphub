package io.github.mirvmir.profile.application.persistence.repository;

import io.github.mirvmir.profile.application.service.port.repository.ProfileRepository;
import io.github.mirvmir.profile.domain.Profile;
import io.github.mirvmir.profile.application.persistence.entity.ProfileEntity;
import io.github.mirvmir.profile.application.persistence.mapper.ProfileMapper;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class HibernateProfileRepository implements ProfileRepository {

    private final SessionFactory sessionFactory;
    private final ProfileMapper profileMapper;

    @Override
    public Profile save(Profile profile) {
        Session session = sessionFactory.getCurrentSession();

        ProfileEntity entity = profileMapper.toEntity(profile);

        if (entity.getId() == null) {
            session.persist(entity);
        } else {
            entity = session.merge(entity);
        }

        return profileMapper.toDomain(entity);
    }

    @Override
    public Profile findByUserId(Long userId) {
        Session session = sessionFactory.getCurrentSession();

        ProfileEntity entity = session.createQuery("""
                from ProfileEntity p
                where p.userId = :userId
                """, ProfileEntity.class)
                .setParameter("userId", userId)
                .uniqueResult();

        return profileMapper.toDomain(entity);
    }

    @Override
    public boolean existsAvatarByFileId(Long fileId) {
        Session session = sessionFactory.getCurrentSession();

        return Boolean.TRUE.equals(
                session.createQuery("""
                        select count(p.id) > 0
                        from ProfileEntity p
                        where p.avatarFileId = :fileId
                        """, Boolean.class)
                        .setParameter("fileId", fileId)
                        .uniqueResult()
        );
    }
}
