package io.github.mirvmir.catalog.application.service.implementation;

import io.github.mirvmir.activity.api.event.ActivityPublishedEvent;
import io.github.mirvmir.catalog.application.service.dto.CatalogTaxonomyData;
import io.github.mirvmir.catalog.application.service.port.repository.ActivityCatalogRepository;
import io.github.mirvmir.catalog.domain.ActivityCatalog;
import io.github.mirvmir.catalog.domain.Format;
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

class DefaultActivityCatalogServiceTest {

    private ActivityCatalogRepository activityCatalogRepository;
    private ProfileApi profileApi;
    private ReviewApi reviewApi;
    private DefaultActivityCatalogService service;
    private CatalogTaxonomyResolver catalogTaxonomyResolver;

    @BeforeEach
    void setUp() {
        activityCatalogRepository = mock(ActivityCatalogRepository.class);
        profileApi = mock(ProfileApi.class);
        reviewApi = mock(ReviewApi.class);
        catalogTaxonomyResolver = mock(CatalogTaxonomyResolver.class);

        service = new DefaultActivityCatalogService(
                activityCatalogRepository,
                profileApi,
                reviewApi,
                catalogTaxonomyResolver
        );
    }

    @Test
    void addActivityToCatalog_shouldCreateActivityCatalog() {
        ActivityPublishedEvent event = mock(ActivityPublishedEvent.class);
        ProfileNameDto profileName = mock(ProfileNameDto.class);
        ReviewRatingInfoResponse ratingInfo =
                mock(ReviewRatingInfoResponse.class);
        CatalogTaxonomyData taxonomyData =
                mock(CatalogTaxonomyData.class);

        Set<Long> topicIds = Set.of(100L, 200L);
        Set<Long> sectionIds = Set.of();
        Set<Long> subjectIds = Set.of(300L);

        when(event.activityId()).thenReturn(10L);
        when(event.title()).thenReturn("Java занятие");
        when(event.authorId()).thenReturn(2L);
        when(event.shortDescription()).thenReturn("Описание занятия");
        when(event.priceAmount()).thenReturn(new BigDecimal("1500"));
        when(event.priceCurrency()).thenReturn(Currency.getInstance("RUB"));
        when(event.activityType()).thenReturn("INDIVIDUAL");
        when(event.topicIds()).thenReturn(topicIds);

        when(profileApi.getProfileName(2L)).thenReturn(profileName);
        when(profileName.givenName()).thenReturn("Иван");
        when(profileName.familyName()).thenReturn("Иванов");

        when(reviewApi.getRatingInfo(10L, null)).thenReturn(ratingInfo);
        when(ratingInfo.ratingAvg()).thenReturn(4.8);
        when(ratingInfo.reviewCount()).thenReturn(12L);

        when(catalogTaxonomyResolver.resolve(topicIds)).thenReturn(taxonomyData);
        when(taxonomyData.topicIds()).thenReturn(topicIds);
        when(taxonomyData.sectionIds()).thenReturn(sectionIds);
        when(taxonomyData.subjectIds()).thenReturn(subjectIds);

        service.addActivityToCatalog(event);

        ArgumentCaptor<ActivityCatalog> captor =
                ArgumentCaptor.forClass(ActivityCatalog.class);

        verify(activityCatalogRepository).saveOrUpdate(captor.capture());

        ActivityCatalog saved = captor.getValue();

        assertEquals(10L, saved.getActivityId());
        assertEquals("Java занятие", saved.getTitle());
        assertEquals("Иван Иванов", saved.getAuthorName());
        assertEquals("Описание занятия", saved.getShortDescription());
        assertEquals(new BigDecimal("1500"), saved.getPrice().getAmount());
        assertEquals(Currency.getInstance("RUB"), saved.getPrice().getCurrency());
        assertEquals(4.8, saved.getRatingAvg());
        assertEquals(12L, saved.getReviewCount());
        assertEquals(Format.INDIVIDUAL, saved.getFormat());
        assertEquals(topicIds, saved.getTopicIds());
        assertEquals(sectionIds, saved.getSectionIds());
        assertEquals(subjectIds, saved.getSubjectIds());

        verify(reviewApi).getRatingInfo(10L, null);
        verify(catalogTaxonomyResolver).resolve(topicIds);
        verify(profileApi).getProfileName(2L);
    }

    @Test
    void removeActivityFromCatalog_shouldDeleteByActivityId() {
        service.removeActivityFromCatalog(10L);

        verify(activityCatalogRepository).deleteByActivityId(10L);
    }

    @Test
    void updateTopicIds_shouldUpdateActivityTopics() {
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

        service.updateTopicIds(10L, topicIds);

        verify(catalogTaxonomyResolver).resolve(topicIds);

        verify(activityCatalogRepository).updateTaxonomyIds(
                10L,
                resolvedTopicIds,
                sectionIds,
                subjectIds
        );
    }
}