package io.github.mirvmir.review.application.persistence.repository;

import io.github.mirvmir.review.application.service.port.repository.ReviewRepository;
import io.github.mirvmir.review.domain.Review;
import io.github.mirvmir.review.domain.ReviewStatus;
import io.github.mirvmir.review.domain.ReviewTargetType;
import io.github.mirvmir.review.application.persistence.entity.ReviewEntity;
import io.github.mirvmir.review.application.persistence.mapper.ReviewMapper;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

@AllArgsConstructor
@Repository
public class HibernateReviewRepository implements ReviewRepository {

    private final SessionFactory sessionFactory;
    private final ReviewMapper reviewMapper;

    @Override
    public Review save(Review review) {
        Session session = sessionFactory.getCurrentSession();

        ReviewEntity entity = reviewMapper.toEntity(review);

        if (entity.getId() == null) {
            session.persist(entity);
        } else {
            entity = session.merge(entity);
        }

        return reviewMapper.toDomain(entity);
    }

    @Override
    public Review findById(Long id) {
        Session session = sessionFactory.getCurrentSession();

        ReviewEntity entity = session.get(ReviewEntity.class, id);

        return reviewMapper.toDomain(entity);
    }

    @Override
    public Review findByTargetAndUser(Long toItemId,
                                      Long fromUserId,
                                      ReviewTargetType targetType) {
        Session session = sessionFactory.getCurrentSession();

        ReviewEntity entity = session.createQuery("""
                select r
                from ReviewEntity r
                where r.toItemId = :toItemId
                  and r.fromUserId = :fromUserId
                  and r.targetType = :targetType
                """, ReviewEntity.class)
                .setParameter("toItemId", toItemId)
                .setParameter("fromUserId", fromUserId)
                .setParameter("targetType", targetType)
                .setMaxResults(1)
                .uniqueResult();

        return reviewMapper.toDomain(entity);
    }

    @Override
    public Long countPublishedReviews(Long toItemId,
                                      ReviewTargetType targetType) {
        Session session = sessionFactory.getCurrentSession();

        Long count = session.createQuery("""
                select count(r.id)
                from ReviewEntity r
                where r.toItemId = :toItemId
                  and r.targetType = :targetType
                  and r.status = :status
                """, Long.class)
                .setParameter("toItemId", toItemId)
                .setParameter("targetType", targetType)
                .setParameter("status", ReviewStatus.PUBLISHED)
                .uniqueResult();

        return count == null
                ? 0L
                : count;
    }

    @Override
    public Double calculateRatingAvg(Long toItemId,
                                     ReviewTargetType targetType) {
        Session session = sessionFactory.getCurrentSession();

        Double ratingAvg = session.createQuery("""
                select avg(r.score)
                from ReviewEntity r
                where r.toItemId = :toItemId
                  and r.targetType = :targetType
                  and r.status = :status
                """, Double.class)
                .setParameter("toItemId", toItemId)
                .setParameter("targetType", targetType)
                .setParameter("status", ReviewStatus.PUBLISHED)
                .uniqueResult();

        return ratingAvg == null
                ? 0.0
                : ratingAvg;
    }
}