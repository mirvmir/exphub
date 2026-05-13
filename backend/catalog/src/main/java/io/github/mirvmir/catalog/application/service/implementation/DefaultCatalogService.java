package io.github.mirvmir.catalog.application.service.implementation;

import io.github.mirvmir.catalog.domain.ActivityCatalog;
import io.github.mirvmir.catalog.domain.CourseCatalog;
import io.github.mirvmir.catalog.web.response.CatalogItemResponse;
import io.github.mirvmir.catalog.application.service.mapper.CatalogMapper;
import io.github.mirvmir.catalog.application.service.port.repository.ActivityCatalogRepository;
import io.github.mirvmir.catalog.application.service.port.repository.CourseCatalogRepository;
import io.github.mirvmir.catalog.application.service.interfaces.CatalogService;
import io.github.mirvmir.catalog.application.service.dto.CatalogFilterDto;
import io.github.mirvmir.catalog.domain.CatalogType;
import io.github.mirvmir.review.api.ReviewApi;
import io.github.mirvmir.review.api.dto.ReviewRatingInfoResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Slf4j
@Service
public class DefaultCatalogService implements CatalogService {

    private final ReviewApi reviewApi;

    private final CourseCatalogRepository courseCatalogRepository;
    private final ActivityCatalogRepository activityCatalogRepository;

    private final CatalogMapper catalogMapper;

    @Transactional(readOnly = true)
    @Override
    public List<CatalogItemResponse> getCatalog(CatalogFilterDto filter) {
        log.debug("Getting catalog with filter={}", filter);

        List<CatalogItemResponse> result = new ArrayList<>();

        if (filter.type() == null || filter.type() == CatalogType.COURSE) {
            result.addAll(courseCatalogRepository.search(filter)
                    .stream()
                    .map(catalogMapper::fromCourse)
                    .toList());
        }

        if (filter.type() == null || filter.type() == CatalogType.ACTIVITY) {
            result.addAll(activityCatalogRepository.search(filter)
                    .stream()
                    .map(catalogMapper::fromActivity)
                    .toList());
        }

        log.debug("Catalog received: filter={}, resultSize={}", filter, result.size());

        return result;
    }

    @Override
    @Transactional
    public void addScore(Long activityId, Long courseId, Double score) {
        log.info("Updating catalog score: activityId={}, courseId={}, score={}", activityId, courseId, score);

        if (courseId != null) {
            CourseCatalog course = courseCatalogRepository.findByCourseId(courseId);

            if (course == null) {
                log.warn("Course catalog item not found for score update: courseId={}", courseId);

                return;
            }

            ReviewRatingInfoResponse ratingInfo = reviewApi.getRatingInfo(
                    null,
                    courseId
            );

            course.updateRatingAvg(ratingInfo.ratingAvg());
            course.updateReviewCount(ratingInfo.reviewCount());

            courseCatalogRepository.saveOrUpdate(course);

            log.info(
                    "Course catalog score updated: courseId={}, ratingAvg={}, reviewCount={}",
                    courseId,
                    ratingInfo.ratingAvg(),
                    ratingInfo.reviewCount()
            );

            return;
        }

        if (activityId != null) {
            ActivityCatalog activity = activityCatalogRepository.findByActivityId(activityId);

            if (activity == null) {
                log.warn("Activity catalog item not found for score update: activityId={}", activityId);

                return;
            }

            ReviewRatingInfoResponse ratingInfo = reviewApi.getRatingInfo(
                    activityId,
                    null
            );

            activity.updateRatingAvg(ratingInfo.ratingAvg());
            activity.updateReviewCount(ratingInfo.reviewCount());

            activityCatalogRepository.saveOrUpdate(activity);

            log.info(
                    "Activity catalog score updated: activityId={}, ratingAvg={}, reviewCount={}",
                    activityId,
                    ratingInfo.ratingAvg(),
                    ratingInfo.reviewCount()
            );

            return;
        }

        log.error("Catalog score update failed: activityId and courseId are null");

        throw new IllegalStateException("Не добавлен id занятия или курса");
    }
}
