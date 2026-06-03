package io.github.mirvmir.catalog.application.service.implementation;

import io.github.mirvmir.catalog.application.service.dto.CatalogFilterDto;
import io.github.mirvmir.catalog.application.service.mapper.CatalogMapper;
import io.github.mirvmir.catalog.application.service.port.repository.ActivityCatalogRepository;
import io.github.mirvmir.catalog.application.service.port.repository.CourseCatalogRepository;
import io.github.mirvmir.catalog.domain.ActivityCatalog;
import io.github.mirvmir.catalog.domain.CatalogType;
import io.github.mirvmir.catalog.domain.CourseCatalog;
import io.github.mirvmir.catalog.domain.Format;
import io.github.mirvmir.catalog.web.response.CatalogItemResponse;
import io.github.mirvmir.review.api.ReviewApi;
import io.github.mirvmir.review.api.dto.ReviewRatingInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultCatalogServiceTest {

    private ReviewApi reviewApi;
    private CourseCatalogRepository courseCatalogRepository;
    private ActivityCatalogRepository activityCatalogRepository;
    private DefaultCatalogService service;

    @BeforeEach
    void setUp() {
        reviewApi = mock(ReviewApi.class);
        courseCatalogRepository = mock(CourseCatalogRepository.class);
        activityCatalogRepository = mock(ActivityCatalogRepository.class);

        CatalogMapper catalogMapper = new CatalogMapper() {
        };

        service = new DefaultCatalogService(
                reviewApi,
                courseCatalogRepository,
                activityCatalogRepository,
                catalogMapper
        );
    }

    @Test
    void getCatalog_shouldReturnCoursesAndActivities_whenTypeIsNull() {
        CatalogFilterDto filter = new CatalogFilterDto(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        CourseCatalog course = course();
        ActivityCatalog activity = activity();

        when(courseCatalogRepository.search(filter))
                .thenReturn(List.of(course));
        when(activityCatalogRepository.search(filter))
                .thenReturn(List.of(activity));

        List<CatalogItemResponse> result = service.getCatalog(filter);

        assertEquals(2, result.size());

        assertEquals(CatalogType.COURSE, result.get(0).type());
        assertEquals(100L, result.get(0).sourceId());

        assertEquals(CatalogType.ACTIVITY, result.get(1).type());
        assertEquals(200L, result.get(1).sourceId());

        verify(courseCatalogRepository).search(filter);
        verify(activityCatalogRepository).search(filter);
    }

    @Test
    void getCatalog_shouldReturnOnlyCourses_whenTypeCourse() {
        CatalogFilterDto filter = new CatalogFilterDto(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                CatalogType.COURSE
        );

        when(courseCatalogRepository.search(filter))
                .thenReturn(List.of(course()));

        List<CatalogItemResponse> result = service.getCatalog(filter);

        assertEquals(1, result.size());
        assertEquals(CatalogType.COURSE, result.get(0).type());

        verify(courseCatalogRepository).search(filter);
        verifyNoInteractions(activityCatalogRepository);
    }

    @Test
    void getCatalog_shouldReturnOnlyActivities_whenTypeActivity() {
        CatalogFilterDto filter = new CatalogFilterDto(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                CatalogType.ACTIVITY
        );

        when(activityCatalogRepository.search(filter))
                .thenReturn(List.of(activity()));

        List<CatalogItemResponse> result = service.getCatalog(filter);

        assertEquals(1, result.size());
        assertEquals(CatalogType.ACTIVITY, result.get(0).type());

        verify(activityCatalogRepository).search(filter);
        verifyNoInteractions(courseCatalogRepository);
    }

    @Test
    void addScore_shouldUpdateCourseRating_whenCourseIdExists() {
        CourseCatalog course = course();

        ReviewRatingInfoResponse ratingInfo =
                mock(ReviewRatingInfoResponse.class);

        when(courseCatalogRepository.findByCourseId(100L))
                .thenReturn(course);
        when(reviewApi.getRatingInfo(null, 100L))
                .thenReturn(ratingInfo);
        when(ratingInfo.ratingAvg()).thenReturn(4.9);
        when(ratingInfo.reviewCount()).thenReturn(15L);

        service.addScore(null, 100L, 5.0);

        assertEquals(4.9, course.getRatingAvg());
        assertEquals(15L, course.getReviewCount());

        verify(courseCatalogRepository).saveOrUpdate(course);
        verifyNoInteractions(activityCatalogRepository);
    }

    @Test
    void addScore_shouldDoNothing_whenCourseNotFound() {
        when(courseCatalogRepository.findByCourseId(100L))
                .thenReturn(null);

        service.addScore(null, 100L, 5.0);

        verify(courseCatalogRepository, never()).saveOrUpdate(any());
        verifyNoInteractions(reviewApi);
    }

    @Test
    void addScore_shouldUpdateActivityRating_whenActivityIdExists() {
        ActivityCatalog activity = activity();

        ReviewRatingInfoResponse ratingInfo =
                mock(ReviewRatingInfoResponse.class);

        when(activityCatalogRepository.findByActivityId(200L))
                .thenReturn(activity);
        when(reviewApi.getRatingInfo(200L, null))
                .thenReturn(ratingInfo);
        when(ratingInfo.ratingAvg()).thenReturn(4.7);
        when(ratingInfo.reviewCount()).thenReturn(8L);

        service.addScore(200L, null, 5.0);

        assertEquals(4.7, activity.getRatingAvg());
        assertEquals(8L, activity.getReviewCount());

        verify(activityCatalogRepository).saveOrUpdate(activity);
        verifyNoInteractions(courseCatalogRepository);
    }

    @Test
    void addScore_shouldThrowException_whenActivityIdAndCourseIdAreNull() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.addScore(null, null, 5.0)
        );

        assertEquals("Не добавлен id занятия или курса", exception.getMessage());
    }

    private CourseCatalog course() {
        return CourseCatalog.load(
                1L,
                100L,
                "Java курс",
                2L,
                "Иван Иванов",
                "Описание курса",
                new BigDecimal("3000"),
                Currency.getInstance("RUB"),
                4.5,
                10L,
                Set.of(1L),
                Set.of(2L),
                Set.of(3L)
        );
    }

    private ActivityCatalog activity() {
        return ActivityCatalog.load(
                2L,
                200L,
                "Java занятие",
                3L,
                "Петр Петров",
                "Описание занятия",
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                4.3,
                5L,
                Format.INDIVIDUAL,
                Set.of(1L),
                Set.of(2L),
                Set.of(3L)
        );
    }
}