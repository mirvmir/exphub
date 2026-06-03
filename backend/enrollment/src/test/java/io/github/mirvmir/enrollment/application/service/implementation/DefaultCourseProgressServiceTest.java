package io.github.mirvmir.enrollment.application.service.implementation;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.course.api.CourseApi;
import io.github.mirvmir.course.api.dto.CourseLessonInfoResponse;
import io.github.mirvmir.enrollment.application.service.port.repository.CourseEnrollmentRepository;
import io.github.mirvmir.enrollment.application.service.port.repository.StudentLessonProgressRepository;
import io.github.mirvmir.enrollment.domain.CourseEnrollment;
import io.github.mirvmir.enrollment.domain.CourseEnrollmentStatus;
import io.github.mirvmir.enrollment.domain.StudentLessonProgress;
import io.github.mirvmir.enrollment.web.response.CourseProgressResponse;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.practice.api.PracticeApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DefaultCourseProgressServiceTest {

    private IdentityApi identityApi;
    private CourseApi courseApi;
    private PracticeApi practiceApi;
    private CourseEnrollmentRepository courseEnrollmentRepository;
    private StudentLessonProgressRepository studentLessonProgressRepository;

    private DefaultCourseProgressService service;

    private final Instant now = Instant.parse("2026-05-13T10:00:00Z");
    private final Clock clock = Clock.fixed(now, ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        identityApi = mock(IdentityApi.class);
        courseApi = mock(CourseApi.class);
        practiceApi = mock(PracticeApi.class);
        courseEnrollmentRepository = mock(CourseEnrollmentRepository.class);
        studentLessonProgressRepository = mock(StudentLessonProgressRepository.class);

        service = new DefaultCourseProgressService(
                identityApi,
                courseApi,
                practiceApi,
                courseEnrollmentRepository,
                studentLessonProgressRepository,
                clock
        );
    }

    @Test
    void completeLesson_shouldCreateProgressAndUpdateEnrollmentProgress() {
        CourseEnrollment enrollment = payedCourseEnrollment();

        CourseLessonInfoResponse lessonInfo = new CourseLessonInfoResponse(
                100L,
                1000L,
                200L,
                300L,
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                null,
                false,
                Set.of(300L, 301L),
                Set.of(300L, 301L, 302L, 303L)
        );

        when(identityApi.getCurrentUserId()).thenReturn(1L);
        when(courseEnrollmentRepository.findPayedByUserIdAndCourseId(1L, 100L))
                .thenReturn(enrollment);
        when(courseApi.getLessonInfo(300L)).thenReturn(lessonInfo);
        when(studentLessonProgressRepository.existsByEnrollmentIdAndCourseLessonId(10L, 300L))
                .thenReturn(false);
        when(studentLessonProgressRepository.countCompletedByEnrollmentIdAndLessonIds(
                10L,
                lessonInfo.moduleLessonIds()
        )).thenReturn(1L);
        when(studentLessonProgressRepository.countCompletedByEnrollmentIdAndLessonIds(
                10L,
                lessonInfo.courseLessonIds()
        )).thenReturn(1L);
        when(courseEnrollmentRepository.saveOrUpdate(enrollment)).thenReturn(enrollment);

        CourseProgressResponse result = service.completeLesson(100L, 300L);

        assertEquals(10L, result.courseEnrollmentId());
        assertEquals(100L, result.courseId());
        assertEquals(200L, result.courseModuleId());
        assertEquals(300L, result.courseLessonId());
        assertEquals(BigDecimal.valueOf(100), result.lessonProgressPercent());
        assertEquals(new BigDecimal("50.00"), result.moduleProgressPercent());
        assertEquals(new BigDecimal("25.00"), result.courseProgressPercent());

        verify(studentLessonProgressRepository).saveOrUpdate(argThat(actual ->
                actual.getEnrollmentId().equals(10L)
                && actual.getCourseLessonId().equals(300L)
        ));
        verify(courseEnrollmentRepository).saveOrUpdate(enrollment);
    }

    @Test
    void completeLesson_shouldNotCreateProgress_whenLessonAlreadyCompleted() {
        CourseEnrollment enrollment = payedCourseEnrollment();

        CourseLessonInfoResponse lessonInfo = new CourseLessonInfoResponse(
                100L,
                1000L,
                200L,
                300L,
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                null,
                false,
                Set.of(300L),
                Set.of(300L)
        );

        when(identityApi.getCurrentUserId()).thenReturn(1L);
        when(courseEnrollmentRepository.findPayedByUserIdAndCourseId(1L, 100L))
                .thenReturn(enrollment);
        when(courseApi.getLessonInfo(300L)).thenReturn(lessonInfo);
        when(studentLessonProgressRepository.existsByEnrollmentIdAndCourseLessonId(10L, 300L))
                .thenReturn(true);
        when(studentLessonProgressRepository.countCompletedByEnrollmentIdAndLessonIds(anyLong(), anySet()))
                .thenReturn(1L);
        when(courseEnrollmentRepository.saveOrUpdate(enrollment)).thenReturn(enrollment);

        service.completeLesson(100L, 300L);

        verify(studentLessonProgressRepository, never()).saveOrUpdate(any());
        verify(courseEnrollmentRepository).saveOrUpdate(enrollment);
    }

    @Test
    void completeLesson_shouldThrowBusinessException_whenLessonNotOpened() {
        CourseEnrollment enrollment = payedCourseEnrollment();

        CourseLessonInfoResponse lessonInfo = new CourseLessonInfoResponse(
                100L,
                1000L,
                200L,
                300L,
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                now.plusSeconds(3600),
                false,
                Set.of(300L),
                Set.of(300L)
        );

        when(identityApi.getCurrentUserId()).thenReturn(1L);
        when(courseEnrollmentRepository.findPayedByUserIdAndCourseId(1L, 100L))
                .thenReturn(enrollment);
        when(courseApi.getLessonInfo(300L)).thenReturn(lessonInfo);

        assertThrows(BusinessException.class,
                () -> service.completeLesson(100L, 300L));

        verify(studentLessonProgressRepository, never()).saveOrUpdate(any());
        verify(courseEnrollmentRepository, never()).saveOrUpdate(any());
    }

    @Test
    void completeLesson_shouldThrowBusinessException_whenPracticeNotCompleted() {
        CourseEnrollment enrollment = payedCourseEnrollment();

        CourseLessonInfoResponse lessonInfo = new CourseLessonInfoResponse(
                100L,
                1000L,
                200L,
                300L,
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                null,
                true,
                Set.of(300L),
                Set.of(300L)
        );

        when(identityApi.getCurrentUserId()).thenReturn(1L);
        when(courseEnrollmentRepository.findPayedByUserIdAndCourseId(1L, 100L))
                .thenReturn(enrollment);
        when(courseApi.getLessonInfo(300L)).thenReturn(lessonInfo);
        when(practiceApi.isPracticeCompletedByLessonIdAndStudentId(
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                1L))
                .thenReturn(false);

        assertThrows(BusinessException.class,
                () -> service.completeLesson(100L, 300L));

        verify(studentLessonProgressRepository, never()).saveOrUpdate(any());
    }

    @Test
    void completeLesson_shouldAllowPracticeLesson_whenPracticeCompleted() {
        CourseEnrollment enrollment = payedCourseEnrollment();

        CourseLessonInfoResponse lessonInfo = new CourseLessonInfoResponse(
                100L,
                1000L,
                200L,
                300L,
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                null,
                true,
                Set.of(300L),
                Set.of(300L)
        );

        when(identityApi.getCurrentUserId()).thenReturn(1L);
        when(courseEnrollmentRepository.findPayedByUserIdAndCourseId(1L, 100L))
                .thenReturn(enrollment);
        when(courseApi.getLessonInfo(300L)).thenReturn(lessonInfo);
        when(practiceApi.isPracticeCompletedByLessonIdAndStudentId(
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                1L))
                .thenReturn(true);
        when(studentLessonProgressRepository.countCompletedByEnrollmentIdAndLessonIds(anyLong(), anySet()))
                .thenReturn(1L);
        when(courseEnrollmentRepository.saveOrUpdate(enrollment)).thenReturn(enrollment);

        CourseProgressResponse result = service.completeLesson(100L, 300L);

        assertEquals("PAYED", result.enrollmentStatus());
        verify(studentLessonProgressRepository).saveOrUpdate(any(StudentLessonProgress.class));
    }

    private CourseEnrollment payedCourseEnrollment() {
        return CourseEnrollment.load(
                10L,
                100L,
                1000L,
                1L,
                CourseEnrollmentStatus.PAYED,
                BigDecimal.ZERO,
                now.minusSeconds(60)
        );
    }
}