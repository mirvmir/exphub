package io.github.mirvmir.catalog.application.persistence.mapper;

import io.github.mirvmir.catalog.domain.CourseCatalog;
import io.github.mirvmir.catalog.application.persistence.entity.CourseCatalogEntity;
import io.github.mirvmir.common.domain.Money;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseCatalogMapper {

    CourseCatalogEntity toEntity(CourseCatalog courseCatalog);

    default CourseCatalog toDomain(CourseCatalogEntity entity) {
        if (entity == null) {
            return null;
        }

        Money price = entity.getPrice();

        return CourseCatalog.load(
                entity.getId(),
                entity.getCourseId(),
                entity.getTitle(),
                entity.getAuthorName(),
                entity.getShortDescription(),
                price.getAmount(),
                price.getCurrency(),
                entity.getRatingAvg(),
                entity.getReviewCount(),
                entity.getTopicIds(),
                entity.getSectionIds(),
                entity.getSubjectIds()
        );
    }
}