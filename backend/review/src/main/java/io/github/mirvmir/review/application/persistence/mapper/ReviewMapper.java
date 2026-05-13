package io.github.mirvmir.review.application.persistence.mapper;

import io.github.mirvmir.review.domain.Review;
import io.github.mirvmir.review.application.persistence.entity.ReviewEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    ReviewEntity toEntity(Review review);

    default Review toDomain(ReviewEntity entity) {
        if (entity == null) {
            return null;
        }

        return Review.load(
                entity.getId(),
                entity.getComment(),
                entity.getScore(),
                entity.getToItemId(),
                entity.getFromUserId(),
                entity.getTargetType(),
                entity.getStatus()
        );
    }
}