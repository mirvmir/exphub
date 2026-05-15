package io.github.mirvmir.media.application.persistence.repository;

import io.github.mirvmir.media.application.service.port.repository.VideoAssetRepository;
import io.github.mirvmir.media.domain.VideoAsset;
import io.github.mirvmir.media.application.persistence.entity.VideoAssetEntity;
import io.github.mirvmir.media.application.persistence.mapper.VideoAssetMapper;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@AllArgsConstructor
public class HibernateVideoAssetRepository implements VideoAssetRepository {

    private final SessionFactory sessionFactory;
    private final VideoAssetMapper videoAssetMapper;

    @Override
    public VideoAsset save(VideoAsset videoAsset) {
        Session session = sessionFactory.getCurrentSession();

        VideoAssetEntity entity = videoAssetMapper.toEntity(videoAsset);

        if (entity.getId() == null) {
            session.persist(entity);
            videoAsset.assignId(entity.getId());
            return videoAsset;
        }

        VideoAssetEntity merged = session.merge(entity);

        return videoAssetMapper.toDomain(merged);
    }

    @Override
    public long countExistingByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        Session session = sessionFactory.getCurrentSession();

        return session.createQuery("""
                        select count(m.id)
                        from VideoAssetEntity m
                        where m.id in :videoIds
                        """, Long.class)
                .setParameter("videoIds", ids)
                .getSingleResult();
    }

    @Override
    public VideoAsset findById(Long id) {
        Session session = sessionFactory.getCurrentSession();

        VideoAssetEntity entity = session.find(VideoAssetEntity.class, id);

        return videoAssetMapper.toDomain(entity);
    }
}