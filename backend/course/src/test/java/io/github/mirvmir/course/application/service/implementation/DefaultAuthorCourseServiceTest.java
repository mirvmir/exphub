package io.github.mirvmir.course.application.service.implementation;

import io.github.mirvmir.common.domain.ContentStatus;
import io.github.mirvmir.common.domain.ModerationStatus;
import io.github.mirvmir.common.exception.ForbiddenException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.course.api.event.CourseChangeTopicIds;
import io.github.mirvmir.course.api.event.CourseDeleteEvent;
import io.github.mirvmir.course.api.event.CoursePublishedEvent;
import io.github.mirvmir.course.application.service.mapper.CourseResponseMapper;
import io.github.mirvmir.course.application.service.port.event.CourseEventPublisher;
import io.github.mirvmir.course.application.service.port.repository.CourseRepository;
import io.github.mirvmir.course.application.service.port.repository.CourseVersionRepository;
import io.github.mirvmir.course.domain.*;
import io.github.mirvmir.course.domain.content.LessonContent;
import io.github.mirvmir.course.domain.content.LessonContentType;
import io.github.mirvmir.course.domain.content.LessonHtml;
import io.github.mirvmir.course.web.request.*;
import io.github.mirvmir.course.web.response.AuthorCourseResponse;
import io.github.mirvmir.course.web.response.IdResponse;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.profile.api.ProfileApi;
import io.github.mirvmir.profile.api.dto.ProfileNameDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultAuthorCourseServiceTest {

    private IdentityApi identityApi;
    private ProfileApi profileApi;
    private CourseRepository courseRepository;
    private CourseVersionRepository courseVersionRepository;
    private CourseResponseMapper courseResponseMapper;
    private CourseEventPublisher eventPublisher;

    private DefaultAuthorCourseService service;

    @BeforeEach
    void setUp() {
        identityApi = mock(IdentityApi.class);
        profileApi = mock(ProfileApi.class);
        courseRepository = mock(CourseRepository.class);
        courseVersionRepository = mock(CourseVersionRepository.class);
        courseResponseMapper = mock(CourseResponseMapper.class);
        eventPublisher = mock(CourseEventPublisher.class);

        service = new DefaultAuthorCourseService(
                identityApi,
                profileApi,
                courseRepository,
                courseVersionRepository,
                courseResponseMapper,
                eventPublisher
        );
    }

    @Test
    void createCourse_shouldCreateDraftCourse() {
        CreateCourseRequest request = new CreateCourseRequest("Новый курс");

        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(courseRepository.saveOrUpdate(any(Course.class))).thenAnswer(invocation -> {
            Course course = invocation.getArgument(0);
            course.assignId(1L);
            return course;
        });

        IdResponse result = service.createCourse(request);

        assertEquals(1L, result.id());

        ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
        verify(courseRepository).saveOrUpdate(captor.capture());

        Course saved = captor.getValue();
        assertEquals(2L, saved.getAuthorId());
        assertEquals(ContentStatus.DRAFT, saved.getStatus());
        assertEquals("Новый курс", saved.getDraftVersion().getTitle());
    }

    @Test
    void getCourse_shouldReturnDraftCourseForAuthor() {
        Course course = draftCourse();
        CourseVersion draftVersion = course.getDraftVersion();
        ProfileNameDto author = mock(ProfileNameDto.class);
        AuthorCourseResponse expected = mock(AuthorCourseResponse.class);

        when(courseRepository.findById(1L)).thenReturn(course);
        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(courseVersionRepository.findByIdAndCourseIdWithModules(10L, 1L))
                .thenReturn(draftVersion);
        when(profileApi.getProfileName(2L)).thenReturn(author);
        when(courseResponseMapper.toAuthorCourseResponse(
                course,
                draftVersion,
                author,
                true,
                true
        )).thenReturn(expected);

        AuthorCourseResponse result = service.getCourse(1L);

        assertSame(expected, result);
    }

    @Test
    void getCourse_shouldThrowForbidden_whenCurrentUserIsNotAuthor() {
        Course course = draftCourse();

        when(courseRepository.findById(1L)).thenReturn(course);
        when(identityApi.getCurrentUserId()).thenReturn(99L);

        assertThrows(ForbiddenException.class,
                () -> service.getCourse(1L));

        verifyNoInteractions(courseVersionRepository, profileApi, courseResponseMapper);
    }

    @Test
    void getCourse_shouldThrowNotFound_whenCourseNotFound() {
        when(courseRepository.findById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> service.getCourse(1L));

        verifyNoInteractions(identityApi, courseVersionRepository, profileApi);
    }

    @Test
    void updateDraftCourse_shouldUpdateDraftAndReturnResponse() {
        Course course = draftCourse();
        AuthorCourseResponse expected = mock(AuthorCourseResponse.class);
        ProfileNameDto author = mock(ProfileNameDto.class);

        UpdateCourseDraftRequest request = new UpdateCourseDraftRequest(
                "Новое название",
                "Новое кратко",
                "<p>Новое описание</p>",
                new BigDecimal("4500"),
                Currency.getInstance("RUB")
        );

        when(courseRepository.findByIdWithDraftContent(1L)).thenReturn(course);
        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(courseRepository.saveOrUpdate(course)).thenReturn(course);
        when(profileApi.getProfileName(2L)).thenReturn(author);
        when(courseResponseMapper.toAuthorCourseResponse(
                eq(course),
                eq(course.getDraftVersion()),
                eq(author),
                eq(true),
                eq(true)
        )).thenReturn(expected);

        AuthorCourseResponse result = service.updateDraftCourse(1L, request);

        assertSame(expected, result);
        assertEquals("Новое название", course.getDraftVersion().getTitle());
        assertEquals("Новое кратко", course.getDraftVersion().getShortDescription());
        verify(courseRepository).saveOrUpdate(course);
    }

    @Test
    void updateTopics_shouldSaveAndPublishTopicChangeEvent() {
        Course course = draftCourse();

        when(courseRepository.findById(1L)).thenReturn(course);
        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(courseRepository.saveOrUpdate(course)).thenReturn(course);

        service.updateTopics(1L, new UpdateCourseTopicsRequest(Set.of(11L, 12L)));

        assertEquals(Set.of(11L, 12L), course.getTopicIds());
        verify(courseRepository).saveOrUpdate(course);
        verify(eventPublisher).changeTopic(any(CourseChangeTopicIds.class));
    }

    @Test
    void updateLessonOpensAt_shouldUpdateLessonOpening() {
        Course course = draftCourse();

        var stableLessonId = java.util.UUID.randomUUID();
        Instant opensAt = Instant.parse("2026-05-13T10:00:00Z");

        when(courseRepository.findById(1L)).thenReturn(course);
        when(identityApi.getCurrentUserId()).thenReturn(2L);

        service.updateLessonOpensAt(
                1L,
                stableLessonId,
                new UpdateLessonOpensAtRequest(opensAt)
        );

        assertTrue(course.getLessonOpenings()
                .stream()
                .anyMatch(opening ->
                        stableLessonId.equals(opening.getStableLessonId())
                        && opensAt.equals(opening.getOpensAt())
                ));

        verify(courseRepository).saveOrUpdate(course);
    }

    @Test
    void requestPublication_shouldMoveDraftToPending() {
        Course course = editableDraftCourseWithContent();
        CourseVersion draftVersionWithContent = course.getDraftVersion();

        when(courseRepository.findByIdWithDraftContent(1L)).thenReturn(course);
        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(courseVersionRepository.findByIdAndCourseIdWithModules(
                draftVersionWithContent.getId(),
                course.getId()
        )).thenReturn(draftVersionWithContent);

        service.requestPublication(1L);

        assertEquals(ModerationStatus.PENDING, course.getDraftVersion().getStatus());
        verify(courseRepository).saveOrUpdate(course);
    }

    @Test
    void archive_shouldArchiveAndPublishDeleteEvent() {
        Course course = activeCourse();

        when(courseRepository.findById(1L)).thenReturn(course);
        when(identityApi.getCurrentUserId()).thenReturn(2L);

        service.archive(1L);

        assertEquals(ContentStatus.ARCHIVED, course.getStatus());
        verify(courseRepository).saveOrUpdate(course);
        verify(eventPublisher).delete(any(CourseDeleteEvent.class));
    }

    @Test
    void unarchive_shouldActivateAndPublishCourseEvent() {
        Course course = archivedCourse();

        when(courseRepository.findById(1L)).thenReturn(course);
        when(identityApi.getCurrentUserId()).thenReturn(2L);

        service.unarchive(1L);

        assertEquals(ContentStatus.ACTIVE, course.getStatus());
        verify(courseRepository).saveOrUpdate(course);
        verify(eventPublisher).publish(any(CoursePublishedEvent.class));
    }

    @Test
    void deleteCourse_shouldDeleteAndPublishDeleteEvent() {
        Course course = activeCourse();

        when(courseRepository.findById(1L)).thenReturn(course);
        when(identityApi.getCurrentUserId()).thenReturn(2L);

        service.deleteCourse(1L);

        assertEquals(ContentStatus.DELETED, course.getStatus());
        verify(courseRepository).saveOrUpdate(course);
        verify(eventPublisher).delete(any(CourseDeleteEvent.class));
    }

    private Course draftCourse() {
        CourseVersion draftVersion = CourseVersion.load(
                10L,
                ModerationStatus.DRAFT,
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
                Set.of(),
                Set.of(),
                null,
                draftVersion
        );
    }

    private Course editableDraftCourseWithContent() {
        CourseVersion draftVersion = CourseVersion.load(
                10L,
                ModerationStatus.DRAFT,
                "Курс",
                "Кратко",
                "<p>Описание</p>",
                new BigDecimal("3000"),
                Currency.getInstance("RUB"),
                null,
                List.of(
                        CourseModule.load(
                                100L,
                                UUID.randomUUID(),
                                "Модуль 1",
                                0,
                                List.of(
                                        CourseLesson.load(
                                                200L,
                                                UUID.randomUUID(),
                                                "Урок 1",
                                                LessonType.THEORY,
                                                0,
                                                List.of(
                                                        LessonBlock.load(
                                                                300L,
                                                                UUID.randomUUID(),
                                                                LessonHtml.load(
                                                                        "<p>Текст урока</p>"
                                                                ),
                                                                0,
                                                                "content-hash"
                                                        )
                                                )
                                        )
                                )
                        )
                )
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

        CourseVersion draftVersion = CourseVersion.load(
                10L,
                ModerationStatus.DRAFT,
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
                draftVersion
        );
    }

    private Course archivedCourse() {
        Course course = activeCourse();
        course.archive();
        return course;
    }
}