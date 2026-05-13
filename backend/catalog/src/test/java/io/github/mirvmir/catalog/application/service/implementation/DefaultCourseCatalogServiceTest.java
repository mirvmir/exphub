package io.github.mirvmir.catalog.application.service.implementation;

import io.github.mirvmir.catalog.application.service.dto.CatalogTaxonomyData;
import io.github.mirvmir.catalog.application.service.port.repository.CourseCatalogRepository;
import io.github.mirvmir.catalog.domain.CourseCatalog;
import io.github.mirvmir.course.api.event.CoursePublishedEvent;
import io.github.mirvmir.profile.api.ProfileApi;
import io.github.mirvmir.profile.api.dto.ProfileNameDto;
import io.github.mirvmir.review.api.ReviewApi;
import io.github.mirvmir.review.api.dto.ReviewRatingInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DefaultCourseCatalogServiceTest {

    private CourseCatalogRepository courseCatalogRepository;
    private ProfileApi profileApi;
    private ReviewApi reviewApi;
    private DefaultCourseCatalogService service;
    private CatalogTaxonomyResolver catalogTaxonomyResolver;

    @BeforeEach
    void setUp() {
        courseCatalogRepository = mock(CourseCatalogRepository.class);
        profileApi = mock(ProfileApi.class);
        reviewApi = mock(ReviewApi.class);
        catalogTaxonomyResolver = mock(CatalogTaxonomyResolver.class);

        service = new DefaultCourseCatalogService(
                courseCatalogRepository,
                profileApi,
                reviewApi,
                catalogTaxonomyResolver
        );
    }

    @Test
    void addCourseToCatalog_shouldCreateCourseCatalog() {
        CoursePublishedEvent event = mock(CoursePublishedEvent.class);
        ProfileNameDto profileName = mock(ProfileNameDto.class);
        ReviewRatingInfoResponse ratingInfo =
                mock(ReviewRatingInfoResponse.class);
        CatalogTaxonomyData taxonomyData =
                mock(CatalogTaxonomyData.class);

        Set<Long> topicIds = Set.of(100L, 200L);
        Set<Long> sectionIds = Set.of();
        Set<Long> subjectIds = Set.of();

        when(event.courseId()).thenReturn(20L);
        when(event.title()).thenReturn("Java курс");
        when(event.authorId()).thenReturn(2L);
        when(event.shortDescription()).thenReturn("Описание курса");
        when(event.priceAmount()).thenReturn(new BigDecimal("3000"));
        when(event.priceCurrency()).thenReturn(Currency.getInstance("RUB"));
        when(event.topicIds()).thenReturn(topicIds);

        when(profileApi.getProfileName(2L)).thenReturn(profileName);
        when(profileName.givenName()).thenReturn("Иван");
        when(profileName.familyName()).thenReturn("Иванов");

        when(reviewApi.getRatingInfo(null, 20L))
                .thenReturn(ratingInfo);
        when(ratingInfo.ratingAvg()).thenReturn(4.9);
        when(ratingInfo.reviewCount()).thenReturn(15L);

        when(catalogTaxonomyResolver.resolve(topicIds))
                .thenReturn(taxonomyData);

        when(taxonomyData.topicIds()).thenReturn(topicIds);
        when(taxonomyData.sectionIds()).thenReturn(sectionIds);
        when(taxonomyData.subjectIds()).thenReturn(subjectIds);

        service.addCourseToCatalog(event);

        ArgumentCaptor<CourseCatalog> captor =
                ArgumentCaptor.forClass(CourseCatalog.class);

        verify(courseCatalogRepository).saveOrUpdate(captor.capture());

        CourseCatalog saved = captor.getValue();

        assertEquals(20L, saved.getCourseId());
        assertEquals("Java курс", saved.getTitle());
        assertEquals("Иван Иванов", saved.getAuthorName());
        assertEquals("Описание курса", saved.getShortDescription());
        assertEquals(new BigDecimal("3000"), saved.getPrice().getAmount());
        assertEquals(Currency.getInstance("RUB"), saved.getPrice().getCurrency());
        assertEquals(4.9, saved.getRatingAvg());
        assertEquals(15L, saved.getReviewCount());
        assertEquals(topicIds, saved.getTopicIds());
        assertEquals(sectionIds, saved.getSectionIds());
        assertEquals(subjectIds, saved.getSubjectIds());

        verify(reviewApi).getRatingInfo(null, 20L);
        verify(catalogTaxonomyResolver).resolve(topicIds);
        verify(profileApi).getProfileName(2L);
    }

    @Test
    void removeCourseFromCatalog_shouldDeleteByCourseId() {
        service.removeCourseFromCatalog(20L);

        verify(courseCatalogRepository).deleteByCourseId(20L);
    }

    @Test
    void updateTopicIds_shouldUpdateCourseTopics() {
        Set<Long> topicIds = Set.of(1L, 2L, 3L);
        Set<Long> resolvedTopicIds = Set.of(1L, 2L, 3L);
        Set<Long> sectionIds = Set.of(10L, 20L);
        Set<Long> subjectIds = Set.of(100L);

        CatalogTaxonomyData taxonomyData =
                mock(CatalogTaxonomyData.class);

        when(catalogTaxonomyResolver.resolve(topicIds))
                .thenReturn(taxonomyData);

        when(taxonomyData.topicIds()).thenReturn(resolvedTopicIds);
        when(taxonomyData.sectionIds()).thenReturn(sectionIds);
        when(taxonomyData.subjectIds()).thenReturn(subjectIds);

        service.updateTopicIds(20L, topicIds);

        verify(catalogTaxonomyResolver).resolve(topicIds);

        verify(courseCatalogRepository).updateTaxonomyIds(
                20L,
                resolvedTopicIds,
                sectionIds,
                subjectIds
        );
    }
}