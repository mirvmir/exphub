package io.github.mirvmir.course.application.service.implementation;

import io.github.mirvmir.common.domain.ContentStatus;
import io.github.mirvmir.common.domain.ModerationStatus;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.course.api.event.CourseDeleteEvent;
import io.github.mirvmir.course.api.event.CoursePublishedEvent;
import io.github.mirvmir.course.application.service.port.event.CourseEventPublisher;
import io.github.mirvmir.course.application.service.port.repository.CourseRepository;
import io.github.mirvmir.course.domain.Course;
import io.github.mirvmir.course.domain.CourseVersion;
import io.github.mirvmir.course.web.request.RejectCourseRequest;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultModerationCourseServiceTest {

    private CourseRepository courseRepository;
    private EnrollmentApi enrollmentApi;
    private CourseEventPublisher eventPublisher;

    private DefaultModerationCourseService service;

    @BeforeEach
    void setUp() {
        courseRepository = mock(CourseRepository.class);
        enrollmentApi = mock(EnrollmentApi.class);
        eventPublisher = mock(CourseEventPublisher.class);

        service = new DefaultModerationCourseService(
                courseRepository,
                enrollmentApi,
                eventPublisher
        );
    }

    @Test
    void approve_shouldApproveCourseAndPublishEvent() {
        Course course = draftCourseForModeration();

        when(courseRepository.findByIdWithDraftContent(1L)).thenReturn(course);

        service.approve(1L);

        assertEquals(ContentStatus.ACTIVE, course.getStatus());
        assertNotNull(course.getPublishedVersion());
        verify(courseRepository).saveOrUpdate(course);
        verify(eventPublisher).publish(any(CoursePublishedEvent.class));
    }

    @Test
    void approve_shouldDeleteOldPublishedEvent_whenCourseAlreadyActive() {
        Course course = activeCourseWithPendingDraft();

        when(courseRepository.findByIdWithDraftContent(1L)).thenReturn(course);

        service.approve(1L);

        verify(eventPublisher).delete(any(CourseDeleteEvent.class));
        verify(eventPublisher).publish(any(CoursePublishedEvent.class));
    }

    @Test
    void reject_shouldRejectDraftVersion() {
        Course course = draftCourseForModeration();

        when(courseRepository.findByIdWithDraftContent(1L)).thenReturn(course);

        service.reject(
                1L,
                new RejectCourseRequest("Нужно доработать")
        );

        assertEquals(ModerationStatus.REJECTED, course.getDraftVersion().getStatus());
        assertEquals("Нужно доработать", course.getDraftVersion().getModerationComment());
        verify(courseRepository).saveOrUpdate(course);
    }

    @Test
    void block_shouldBlockRefundAndDeleteEvent() {
        Course course = activeCourse();

        when(courseRepository.findByIdWithPublishedContent(1L)).thenReturn(course);

        service.block(1L);

        assertEquals(ContentStatus.BLOCKED, course.getStatus());
        verify(courseRepository).saveOrUpdate(course);
        verify(enrollmentApi).refundPayedCourseEnrollments(1L, "Курс заблокирован");
        verify(eventPublisher).delete(any(CourseDeleteEvent.class));
    }

    @Test
    void block_shouldThrowNotFound_whenCourseNotFound() {
        when(courseRepository.findByIdWithPublishedContent(1L)).thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> service.block(1L));

        verifyNoInteractions(enrollmentApi, eventPublisher);
    }

    private Course draftCourseForModeration() {
        CourseVersion draftVersion = CourseVersion.load(
                10L,
                ModerationStatus.PENDING,
                "Курс",
                "Кратко",
                "<p>Описание</p>",
                new BigDecimal("3000"),
                Currency.getInstance("RUB"),
                null,
                null
        );

        return Course.load(
                1L,
                2L,
                ContentStatus.DRAFT,
                Set.of(11L),
                Set.of(),
                null,
                draftVersion
        );
    }

    private Course activeCourse() {
        CourseVersion publishedVersion = CourseVersion.load(
                20L,
                ModerationStatus.APPROVED,
                "Курс",
                "Кратко",
                "<p>Описание</p>",
                new BigDecimal("3000"),
                Currency.getInstance("RUB"),
                null,
                null
        );

        return Course.load(
                1L,
                2L,
                ContentStatus.ACTIVE,
                Set.of(11L),
                Set.of(),
                publishedVersion,
                null
        );
    }

    private Course activeCourseWithPendingDraft() {
        CourseVersion publishedVersion = CourseVersion.load(
                20L,
                ModerationStatus.APPROVED,
                "Старый курс",
                "Старое кратко",
                "<p>Старое описание</p>",
                new BigDecimal("2500"),
                Currency.getInstance("RUB"),
                null,
                null
        );

        CourseVersion draftVersion = CourseVersion.load(
                10L,
                ModerationStatus.PENDING,
                "Новый курс",
                "Новое кратко",
                "<p>Новое описание</p>",
                new BigDecimal("3000"),
                Currency.getInstance("RUB"),
                null,
                null
        );

        return Course.load(
                1L,
                2L,
                ContentStatus.ACTIVE,
                Set.of(11L),
                Set.of(),
                publishedVersion,
                draftVersion
        );
    }
}