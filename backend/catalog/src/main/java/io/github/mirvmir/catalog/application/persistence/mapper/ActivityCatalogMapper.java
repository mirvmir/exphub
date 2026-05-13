package io.github.mirvmir.catalog.application.persistence.mapper;

import io.github.mirvmir.catalog.domain.ActivityCatalog;
import io.github.mirvmir.catalog.application.persistence.entity.ActivityCatalogEntity;
import io.github.mirvmir.common.domain.Money;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActivityCatalogMapper {

    ActivityCatalogEntity toEntity(ActivityCatalog activityCatalog);

    default ActivityCatalog toDomain(ActivityCatalogEntity entity) {
        if (entity == null) {
            return null;
        }

        Money price = entity.getPrice();

        return ActivityCatalog.load(
                entity.getId(),
                entity.getActivityId(),
                entity.getTitle(),
                entity.getAuthorName(),
                entity.getShortDescription(),
                price.getAmount(),
                price.getCurrency(),
                entity.getRatingAvg(),
                entity.getReviewCount(),
                entity.getFormat(),
                entity.getTopicIds(),
                entity.getSectionIds(),
                entity.getSubjectIds()
        );
    }
}