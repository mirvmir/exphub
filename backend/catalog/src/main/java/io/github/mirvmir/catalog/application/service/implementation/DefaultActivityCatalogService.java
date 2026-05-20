package io.github.mirvmir.catalog.application.service.implementation;

import io.github.mirvmir.activity.api.event.ActivityPublishedEvent;
import io.github.mirvmir.catalog.application.service.dto.CatalogTaxonomyData;
import io.github.mirvmir.catalog.application.service.interfaces.ActivityCatalogService;
import io.github.mirvmir.catalog.application.service.port.repository.ActivityCatalogRepository;
import io.github.mirvmir.catalog.domain.ActivityCatalog;
import io.github.mirvmir.catalog.domain.Format;
import io.github.mirvmir.profile.api.ProfileApi;
import io.github.mirvmir.profile.api.dto.ProfileNameDto;
import io.github.mirvmir.review.api.ReviewApi;
import io.github.mirvmir.review.api.dto.ReviewRatingInfoResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@Slf4j
@Service
public class DefaultActivityCatalogService implements ActivityCatalogService {

    private final ActivityCatalogRepository activityCatalogRepository;

    private final ProfileApi profileApi;
    private final ReviewApi reviewApi;
    private final CatalogTaxonomyResolver catalogTaxonomyResolver;

    @Override
    @Transactional
    public void addActivityToCatalog(ActivityPublishedEvent event) {
        log.debug("Adding activity to catalog: activityId={}, authorId={}, activityType={}",
                event.activityId(),
                event.authorId(),
                event.activityType());

        ReviewRatingInfoResponse ratingInfo = reviewApi.getRatingInfo(
                event.activityId(),
                null
        );

        CatalogTaxonomyData taxonomyData =
                catalogTaxonomyResolver.resolve(event.topicIds());

        ActivityCatalog activityCatalog = ActivityCatalog.create(
                event.activityId(),
                event.title(),
                event.authorId(),
                resolveAuthorName(event.authorId()),
                event.shortDescription(),
                event.priceAmount(),
                event.priceCurrency(),
                ratingInfo.ratingAvg(),
                ratingInfo.reviewCount(),
                resolveFormat(event.activityType()),
                taxonomyData.topicIds(),
                taxonomyData.sectionIds(),
                taxonomyData.subjectIds()
        );

        activityCatalogRepository.saveOrUpdate(activityCatalog);
        log.info("Activity added to catalog: activityId={}, ratingAvg={}, reviewCount={}, topicIds={}",
                event.activityId(),
                ratingInfo.ratingAvg(),
                ratingInfo.reviewCount(),
                taxonomyData.topicIds());
    }

    @Override
    @Transactional
    public void removeActivityFromCatalog(Long activityId) {
        log.debug("Removing activity from catalog: activityId={}", activityId);

        activityCatalogRepository.deleteByActivityId(activityId);
        log.info("Activity removed from catalog: activityId={}", activityId);
    }

    @Override
    @Transactional
    public void updateTopicIds(Long activityId, Set<Long> topicIds) {
        log.debug("Updating activity catalog topics: activityId={}, topicIds={}", activityId, topicIds);

        CatalogTaxonomyData taxonomyData =
                catalogTaxonomyResolver.resolve(topicIds);

        activityCatalogRepository.updateTaxonomyIds(
                activityId,
                taxonomyData.topicIds(),
                taxonomyData.sectionIds(),
                taxonomyData.subjectIds()
        );

        log.info("Activity catalog topics updated: activityId={}, topicIds={}, sectionIds={}, subjectIds={}",
                activityId,
                taxonomyData.topicIds(),
                taxonomyData.sectionIds(),
                taxonomyData.subjectIds());
    }

    private Format resolveFormat(String activityType) {
        return Format.valueOf(activityType);
    }

    private String resolveAuthorName(Long authorId) {
        ProfileNameDto name = profileApi.getProfileName(authorId);

        String authorName = Stream.of(name.givenName(), name.familyName())
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "));

        log.info("Resolved activity author name: authorId={}, authorName={}", authorId, authorName);
        return authorName;
    }
}
