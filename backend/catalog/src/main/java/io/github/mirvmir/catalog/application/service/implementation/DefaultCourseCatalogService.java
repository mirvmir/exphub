package io.github.mirvmir.catalog.application.service.implementation;

import io.github.mirvmir.catalog.application.service.dto.CatalogTaxonomyData;
import io.github.mirvmir.catalog.application.service.interfaces.CourseCatalogService;
import io.github.mirvmir.catalog.application.service.port.repository.CourseCatalogRepository;
import io.github.mirvmir.catalog.domain.CourseCatalog;
import io.github.mirvmir.course.api.event.CoursePublishedEvent;
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
public class DefaultCourseCatalogService implements CourseCatalogService {

    private final CourseCatalogRepository courseCatalogRepository;

    private final ProfileApi profileApi;
    private final ReviewApi reviewApi;
    private final CatalogTaxonomyResolver catalogTaxonomyResolver;

    @Override
    @Transactional
    public void addCourseToCatalog(CoursePublishedEvent event) {
        log.info(
                "Adding course to catalog: courseId={}, authorId={}",
                event.courseId(),
                event.authorId()
        );

        ReviewRatingInfoResponse ratingInfo = reviewApi.getRatingInfo(
                null,
                event.courseId()
        );

        CatalogTaxonomyData taxonomyData =
                catalogTaxonomyResolver.resolve(event.topicIds());

        CourseCatalog courseCatalog = CourseCatalog.create(
                event.courseId(),
                event.title(),
                resolveAuthorName(event.authorId()),
                event.shortDescription(),
                event.priceAmount(),
                event.priceCurrency(),
                ratingInfo.ratingAvg(),
                ratingInfo.reviewCount(),
                taxonomyData.topicIds(),
                taxonomyData.sectionIds(),
                taxonomyData.subjectIds()
        );

        courseCatalogRepository.saveOrUpdate(courseCatalog);

        log.info(
                "Course added to catalog: courseId={}, ratingAvg={}, reviewCount={}, topicIds={}",
                event.courseId(),
                ratingInfo.ratingAvg(),
                ratingInfo.reviewCount(),
                taxonomyData.topicIds()
        );
    }

    @Override
    @Transactional
    public void removeCourseFromCatalog(Long courseId) {
        log.info("Removing course from catalog: courseId={}", courseId);

        courseCatalogRepository.deleteByCourseId(courseId);

        log.info("Course removed from catalog: courseId={}", courseId);
    }

    @Override
    @Transactional
    public void updateTopicIds(Long courseId, Set<Long> topicIds) {
        log.info("Updating course catalog topics: courseId={}, topicIds={}", courseId, topicIds);

        CatalogTaxonomyData taxonomyData =
                catalogTaxonomyResolver.resolve(topicIds);

        courseCatalogRepository.updateTaxonomyIds(
                courseId,
                taxonomyData.topicIds(),
                taxonomyData.sectionIds(),
                taxonomyData.subjectIds()
        );

        log.info(
                "Course catalog topics updated: courseId={}, topicIds={}, sectionIds={}, subjectIds={}",
                courseId,
                taxonomyData.topicIds(),
                taxonomyData.sectionIds(),
                taxonomyData.subjectIds()
        );
    }

    private String resolveAuthorName(Long authorId) {
        ProfileNameDto name = profileApi.getProfileName(authorId);

        String authorName = Stream.of(name.givenName(), name.familyName())
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "));

        log.debug("Resolved course author name: authorId={}, authorName={}", authorId, authorName);

        return authorName;
    }
}
