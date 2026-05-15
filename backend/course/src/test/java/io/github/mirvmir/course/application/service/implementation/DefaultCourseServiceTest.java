package io.github.mirvmir.course.application.service.implementation;

import io.github.mirvmir.common.domain.ContentStatus;
import io.github.mirvmir.common.domain.ModerationStatus;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.course.application.service.mapper.CourseResponseMapper;
import io.github.mirvmir.course.application.service.port.repository.CourseRepository;
import io.github.mirvmir.course.domain.Course;
import io.github.mirvmir.course.domain.CourseVersion;
import io.github.mirvmir.course.web.response.CourseInfoResponse;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.profile.api.ProfileApi;
import io.github.mirvmir.profile.api.dto.ProfileNameDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultCourseServiceTest {

    private IdentityApi identityApi;
    private EnrollmentApi enrollmentApi;
    private ProfileApi profileApi;
    private CourseRepository courseRepository;
    private CourseResponseMapper courseResponseMapper;

    private DefaultCourseService service;

    @BeforeEach
    void setUp() {
        identityApi = mock(IdentityApi.class);
        enrollmentApi = mock(EnrollmentApi.class);
        profileApi = mock(ProfileApi.class);
        courseRepository = mock(CourseRepository.class);
        courseResponseMapper = mock(CourseResponseMapper.class);

        service = new DefaultCourseService(
                identityApi,
                enrollmentApi,
                profileApi,
                courseRepository,
                courseResponseMapper
        );
    }

    @Test
    void getCourse_shouldReturnCourseInfo() {
        Course course = activeCourse();
        ProfileNameDto author = mock(ProfileNameDto.class);
        CourseInfoResponse expected = mock(CourseInfoResponse.class);

        when(courseRepository.findById(1L)).thenReturn(course);
        when(profileApi.getProfileName(2L)).thenReturn(author);
        when(identityApi.getCurrentUserId()).thenReturn(5L);
        when(enrollmentApi.isStudentOfCourse(1L, 5L)).thenReturn(true);
        when(courseResponseMapper.toCourseInfoResponse(
                course,
                course.getPublishedVersion(),
                author,
                true
        )).thenReturn(expected);

        CourseInfoResponse result = service.getCourse(1L);

        assertSame(expected, result);
        verify(courseRepository).findById(1L);
        verify(profileApi).getProfileName(2L);
        verify(enrollmentApi).isStudentOfCourse(1L, 5L);
    }

    @Test
    void getCourse_shouldThrowNotFound_whenCourseNotFound() {
        when(courseRepository.findById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> service.getCourse(1L));

        verifyNoInteractions(profileApi, identityApi, enrollmentApi, courseResponseMapper);
    }

    private Course activeCourse() {
        CourseVersion publishedVersion = CourseVersion.load(
                10L,
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
                3L,
                Set.of(11L),
                Set.of(),
                publishedVersion,
                null
        );
    }
}