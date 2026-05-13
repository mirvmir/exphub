package io.github.mirvmir.media.application.persistence.repository;

import io.github.mirvmir.media.application.service.port.repository.MediaFileAssetRepository;
import io.github.mirvmir.media.domain.MediaFileAsset;
import io.github.mirvmir.media.application.persistence.entity.MediaFileAssetEntity;
import io.github.mirvmir.media.application.persistence.mapper.MediaFileAssetMapper;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class HibernateMediaFileAssetRepository implements MediaFileAssetRepository {

    private final SessionFactory sessionFactory;
    private final MediaFileAssetMapper mapper;

    @Override
    public MediaFileAsset findById(Long id) {
        Session session = sessionFactory.getCurrentSession();

        MediaFileAssetEntity entity = session.find(MediaFileAssetEntity.class, id);

        return mapper.toDomain(entity);
    }

    @Override
    public MediaFileAsset save(MediaFileAsset mediaFileAsset) {
        Session session = sessionFactory.getCurrentSession();

        MediaFileAssetEntity entity = mapper.toEntity(mediaFileAsset);

        if (entity.getId() == null) {
            session.persist(entity);
        } else {
            entity = session.merge(entity);
        }

        return mapper.toDomain(entity);
    }
}