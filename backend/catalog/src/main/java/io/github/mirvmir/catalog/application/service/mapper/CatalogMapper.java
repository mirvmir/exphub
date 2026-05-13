package io.github.mirvmir.catalog.application.service.mapper;

import io.github.mirvmir.catalog.web.response.CatalogItemResponse;
import io.github.mirvmir.catalog.domain.ActivityCatalog;
import io.github.mirvmir.catalog.domain.CatalogType;
import io.github.mirvmir.catalog.domain.CourseCatalog;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CatalogMapper {

    default CatalogItemResponse fromCourse(CourseCatalog course) {
        return new CatalogItemResponse(
                CatalogType.COURSE,
                course.getCourseId(),
                course.getTitle(),
                course.getAuthorName(),
                course.getShortDescription(),
                course.getPrice().getAmount(),
                course.getPrice().getCurrency(),
                course.getRatingAvg(),
                null
        );
    }

    default CatalogItemResponse fromActivity(ActivityCatalog activity) {
        return new CatalogItemResponse(
                CatalogType.ACTIVITY,
                activity.getActivityId(),
                activity.getTitle(),
                activity.getAuthorName(),
                activity.getShortDescription(),
                activity.getPrice().getAmount(),
                activity.getPrice().getCurrency(),
                activity.getRatingAvg(),
                activity.getFormat()
        );
    }

    default List<CatalogItemResponse> fromCourses(List<CourseCatalog> courses) {
        return courses.stream()
                .map(this::fromCourse)
                .toList();
    }

    default List<CatalogItemResponse> fromActivities(List<ActivityCatalog> activities) {
        return activities.stream()
                .map(this::fromActivity)
                .toList();
    }
}