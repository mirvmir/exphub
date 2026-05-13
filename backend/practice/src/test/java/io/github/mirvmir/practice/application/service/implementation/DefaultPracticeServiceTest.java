package io.github.mirvmir.practice.application.service.implementation;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.ForbiddenException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.course.api.CourseApi;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import io.github.mirvmir.enrollment.api.dto.StudentCourseEnrollmentResponse;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.practice.application.service.mapper.PracticeSubmissionResponseMapper;
import io.github.mirvmir.practice.application.service.port.repository.PracticeSubmissionAnswerRepository;
import io.github.mirvmir.practice.application.service.port.repository.PracticeSubmissionCommentRepository;
import io.github.mirvmir.practice.application.service.port.repository.PracticeSubmissionRepository;
import io.github.mirvmir.practice.domain.PracticeSubmission;
import io.github.mirvmir.practice.domain.PracticeSubmissionAnswer;
import io.github.mirvmir.practice.domain.PracticeSubmissionComment;
import io.github.mirvmir.practice.web.request.CreatePracticeAnswerRequest;
import io.github.mirvmir.practice.web.request.CreatePracticeCommentRequest;
import io.github.mirvmir.practice.web.response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DefaultPracticeServiceTest {

    private IdentityApi identityApi;
    private EnrollmentApi enrollmentApi;
    private CourseApi courseApi;

    private PracticeSubmissionRepository practiceSubmissionRepository;
    private PracticeSubmissionAnswerRepository practiceSubmissionAnswerRepository;
    private PracticeSubmissionCommentRepository practiceSubmissionCommentRepository;
    private PracticeSubmissionResponseMapper practiceSubmissionResponseMapper;

    private DefaultPracticeService service;

    private final Instant now = Instant.parse("2026-05-13T10:00:00Z");
    private final Clock clock = Clock.fixed(now, ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        identityApi = mock(IdentityApi.class);
        enrollmentApi = mock(EnrollmentApi.class);
        courseApi = mock(CourseApi.class);

        practiceSubmissionRepository = mock(PracticeSubmissionRepository.class);
        practiceSubmissionAnswerRepository = mock(PracticeSubmissionAnswerRepository.class);
        practiceSubmissionCommentRepository = mock(PracticeSubmissionCommentRepository.class);
        practiceSubmissionResponseMapper = mock(PracticeSubmissionResponseMapper.class);

        service = new DefaultPracticeService(
                identityApi,
                enrollmentApi,
                courseApi,
                practiceSubmissionRepository,
                practiceSubmissionAnswerRepository,
                practiceSubmissionCommentRepository,
                practiceSubmissionResponseMapper,
                clock
        );
    }

    @Test
    void addAnswer_shouldCreateSubmissionAndAnswer_whenSubmissionDoesNotExist() {
        when(identityApi.getCurrentUserId()).thenReturn(1L);
        when(courseApi.isPractice(100L)).thenReturn(true);
        when(practiceSubmissionRepository.findByLessonIdAndStudentId(100L, 1L))
                .thenReturn(null);

        when(enrollmentApi.getStudentCourseEnrollment(1L, 100L))
                .thenReturn(new StudentCourseEnrollmentResponse(
                        200L,
                        1L,
                        100L,
                        2L));

        when(practiceSubmissionRepository.saveOrUpdate(any(PracticeSubmission.class)))
                .thenAnswer(invocation -> {
                    PracticeSubmission submission = invocation.getArgument(0);
                    submission.assignId(10L);
                    return submission;
                });

        when(practiceSubmissionAnswerRepository.saveOrUpdate(any(PracticeSubmissionAnswer.class)))
                .thenAnswer(invocation -> {
                    PracticeSubmissionAnswer answer = invocation.getArgument(0);
                    answer.assignId(1000L);
                    return answer;
                });

        PracticeAnswerResponse result = service.addAnswer(
                100L,
                new CreatePracticeAnswerRequest("<p>Ответ</p>", 50L)
        );

        assertEquals(1000L, result.id());
        assertEquals(10L, result.practiceSubmissionId());
        assertEquals("<p>Ответ</p>", result.html());
        assertEquals(50L, result.fileId());
        assertEquals(now, result.createdAt());

        verify(practiceSubmissionRepository).saveOrUpdate(argThat(submission ->
                submission.getLessonId().equals(100L)
                && submission.getCourseEnrollmentId().equals(200L)
                && submission.getStudentId().equals(1L)
                && submission.getCreatedAt().equals(now)
                && submission.getCheckedAt() == null
        ));

        verify(practiceSubmissionAnswerRepository).saveOrUpdate(argThat(answer ->
                answer.getPracticeSubmissionId().equals(10L)
                && answer.getHtml().equals("<p>Ответ</p>")
                && answer.getFileId().equals(50L)
                && answer.getCreatedAt().equals(now)
        ));
    }

    @Test
    void addAnswer_shouldCreateAnswer_whenSubmissionAlreadyExists() {
        PracticeSubmission submission = activeSubmission();

        when(identityApi.getCurrentUserId()).thenReturn(1L);
        when(courseApi.isPractice(100L)).thenReturn(true);
        when(practiceSubmissionRepository.findByLessonIdAndStudentId(100L, 1L))
                .thenReturn(submission);

        when(practiceSubmissionAnswerRepository.saveOrUpdate(any(PracticeSubmissionAnswer.class)))
                .thenAnswer(invocation -> {
                    PracticeSubmissionAnswer answer = invocation.getArgument(0);
                    answer.assignId(1000L);
                    return answer;
                });

        PracticeAnswerResponse result = service.addAnswer(
                100L,
                new CreatePracticeAnswerRequest("<p>Ответ</p>", 50L)
        );

        assertEquals(1000L, result.id());
        assertEquals(10L, result.practiceSubmissionId());
        assertEquals("<p>Ответ</p>", result.html());

        verify(practiceSubmissionRepository, never())
                .saveOrUpdate(any(PracticeSubmission.class));
    }

    @Test
    void addAnswer_shouldThrowBusinessException_whenLessonIsNotPractice() {
        when(identityApi.getCurrentUserId()).thenReturn(1L);
        when(courseApi.isPractice(100L)).thenReturn(false);

        assertThrows(BusinessException.class,
                () -> service.addAnswer(
                        100L,
                        new CreatePracticeAnswerRequest("<p>Ответ</p>", 50L)
                ));

        verifyNoInteractions(practiceSubmissionRepository);
        verifyNoInteractions(practiceSubmissionAnswerRepository);
    }

    @Test
    void addAnswer_shouldThrowForbidden_whenStudentHasNoEnrollment() {
        when(identityApi.getCurrentUserId()).thenReturn(1L);
        when(courseApi.isPractice(100L)).thenReturn(true);
        when(practiceSubmissionRepository.findByLessonIdAndStudentId(100L, 1L))
                .thenReturn(null);
        when(enrollmentApi.getStudentCourseEnrollment(1L, 100L))
                .thenReturn(null);

        assertThrows(ForbiddenException.class,
                () -> service.addAnswer(
                        100L,
                        new CreatePracticeAnswerRequest("<p>Ответ</p>", 50L)
                ));

        verify(practiceSubmissionAnswerRepository, never())
                .saveOrUpdate(any(PracticeSubmissionAnswer.class));
    }

    @Test
    void addAnswer_shouldThrowBusinessException_whenSubmissionAlreadyChecked() {
        PracticeSubmission submission = checkedSubmission();

        when(identityApi.getCurrentUserId()).thenReturn(1L);
        when(courseApi.isPractice(100L)).thenReturn(true);
        when(practiceSubmissionRepository.findByLessonIdAndStudentId(100L, 1L))
                .thenReturn(submission);

        assertThrows(BusinessException.class,
                () -> service.addAnswer(
                        100L,
                        new CreatePracticeAnswerRequest("<p>Ответ</p>", 50L)
                ));

        verify(practiceSubmissionAnswerRepository, never())
                .saveOrUpdate(any(PracticeSubmissionAnswer.class));
    }

    @Test
    void addComment_shouldCreateCommentByTeacher() {
        PracticeSubmissionAnswer answer = answer();
        PracticeSubmission submission = activeSubmission();

        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(practiceSubmissionAnswerRepository.findById(1000L)).thenReturn(answer);
        when(practiceSubmissionRepository.findById(10L)).thenReturn(submission);
        when(enrollmentApi.isTeacherOfCourseEnrollment(2L, 200L)).thenReturn(true);

        when(practiceSubmissionCommentRepository.saveOrUpdate(any(PracticeSubmissionComment.class)))
                .thenAnswer(invocation -> {
                    PracticeSubmissionComment comment = invocation.getArgument(0);
                    comment.assignId(5000L);
                    return comment;
                });

        PracticeCommentResponse result = service.addComment(
                1000L,
                new CreatePracticeCommentRequest("<p>Комментарий</p>", 60L)
        );

        assertEquals(5000L, result.id());
        assertEquals(1000L, result.practiceSubmissionAnswerId());
        assertEquals("<p>Комментарий</p>", result.html());
        assertEquals(60L, result.fileId());
        assertEquals(now, result.createdAt());
    }

    @Test
    void addComment_shouldThrowNotFound_whenAnswerNotFound() {
        when(practiceSubmissionAnswerRepository.findById(1000L)).thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> service.addComment(
                        1000L,
                        new CreatePracticeCommentRequest("<p>Комментарий</p>", 60L)
                ));

        verifyNoInteractions(practiceSubmissionRepository);
        verifyNoInteractions(practiceSubmissionCommentRepository);
    }

    @Test
    void addComment_shouldThrowForbidden_whenTeacherIsNotTeacherOfEnrollment() {
        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(practiceSubmissionAnswerRepository.findById(1000L)).thenReturn(answer());
        when(practiceSubmissionRepository.findById(10L)).thenReturn(activeSubmission());
        when(enrollmentApi.isTeacherOfCourseEnrollment(2L, 200L)).thenReturn(false);

        assertThrows(ForbiddenException.class,
                () -> service.addComment(
                        1000L,
                        new CreatePracticeCommentRequest("<p>Комментарий</p>", 60L)
                ));

        verify(practiceSubmissionCommentRepository, never())
                .saveOrUpdate(any(PracticeSubmissionComment.class));
    }

    @Test
    void checkSubmissionByTeacher_shouldCheckSubmission() {
        PracticeSubmission submission = activeSubmission();

        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(practiceSubmissionRepository.findById(10L)).thenReturn(submission);
        when(enrollmentApi.isTeacherOfCourseEnrollment(2L, 200L)).thenReturn(true);
        when(practiceSubmissionRepository.saveOrUpdate(submission)).thenReturn(submission);

        PracticeSubmissionResponse result = service.checkSubmissionByTeacher(10L);

        assertEquals(10L, result.id());
        assertEquals(100L, result.lessonId());
        assertEquals(200L, result.courseEnrollmentId());
        assertEquals(1L, result.studentId());
        assertEquals(now, result.checkedAt());

        verify(practiceSubmissionRepository).saveOrUpdate(submission);
    }

    @Test
    void getMySubmission_shouldReturnEmptySubmission_whenSubmissionDoesNotExist() {
        when(identityApi.getCurrentUserId()).thenReturn(1L);
        when(enrollmentApi.getStudentCourseEnrollment(1L, 100L))
                .thenReturn(new StudentCourseEnrollmentResponse(
                        200L,
                        1L,
                        100L,
                        2L));
        when(practiceSubmissionRepository.findByLessonIdAndStudentId(100L, 1L))
                .thenReturn(null);

        PracticeSubmissionDetailsResponse result = service.getMySubmission(100L);

        assertNull(result.id());
        assertEquals(100L, result.lessonId());
        assertEquals(200L, result.courseEnrollmentId());
        assertEquals(1L, result.studentId());
        assertTrue(result.answers().isEmpty());

        verifyNoInteractions(practiceSubmissionAnswerRepository);
        verifyNoInteractions(practiceSubmissionCommentRepository);
    }

    @Test
    void getMySubmission_shouldReturnSubmissionWithAnswersAndComments() {
        PracticeSubmission submission = activeSubmission();
        PracticeSubmissionAnswer answer = answer();
        PracticeSubmissionComment comment = comment();

        PracticeAnswerDetailsResponse answerResponse =
                new PracticeAnswerDetailsResponse(
                        1000L,
                        10L,
                        "<p>Ответ</p>",
                        50L,
                        answer.getCreatedAt(),
                        List.of(new PracticeCommentResponse(
                                5000L,
                                1000L,
                                "<p>Комментарий</p>",
                                60L,
                                comment.getCreatedAt()
                        ))
                );

        PracticeSubmissionDetailsResponse expected =
                new PracticeSubmissionDetailsResponse(
                        10L,
                        100L,
                        200L,
                        1L,
                        submission.getCreatedAt(),
                        null,
                        List.of(answerResponse)
                );

        when(identityApi.getCurrentUserId()).thenReturn(1L);
        when(enrollmentApi.getStudentCourseEnrollment(1L, 100L))
                .thenReturn(new StudentCourseEnrollmentResponse(
                        200L,
                        1L,
                        100L,
                        2L));
        when(practiceSubmissionRepository.findByLessonIdAndStudentId(100L, 1L))
                .thenReturn(submission);
        when(practiceSubmissionAnswerRepository.findByPracticeSubmissionId(10L))
                .thenReturn(List.of(answer));
        when(practiceSubmissionCommentRepository.findByPracticeSubmissionAnswerIds(List.of(1000L)))
                .thenReturn(List.of(comment));
        when(practiceSubmissionResponseMapper.toAnswerResponse(answer, List.of(comment)))
                .thenReturn(answerResponse);
        when(practiceSubmissionResponseMapper.toSubmissionResponse(submission, List.of(answerResponse)))
                .thenReturn(expected);

        PracticeSubmissionDetailsResponse result = service.getMySubmission(100L);

        assertSame(expected, result);
    }

    @Test
    void getLessonSubmissionsForTeacher_shouldReturnSubmissions() {
        PracticeSubmission submission = activeSubmission();
        PracticeSubmissionAnswer answer = answer();
        PracticeSubmissionComment comment = comment();

        PracticeSubmissionDetailsResponse expected =
                new PracticeSubmissionDetailsResponse(
                        10L,
                        100L,
                        200L,
                        1L,
                        submission.getCreatedAt(),
                        null,
                        List.of()
                );

        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(courseApi.isPractice(100L)).thenReturn(true);
        when(practiceSubmissionRepository.findByLessonId(100L))
                .thenReturn(List.of(submission));
        when(enrollmentApi.isTeacherOfCourseEnrollment(2L, 200L))
                .thenReturn(true);
        when(practiceSubmissionAnswerRepository.findByPracticeSubmissionIds(List.of(10L)))
                .thenReturn(List.of(answer));
        when(practiceSubmissionCommentRepository.findByPracticeSubmissionAnswerIds(List.of(1000L)))
                .thenReturn(List.of(comment));
        when(practiceSubmissionResponseMapper.toSubmissionResponses(
                eq(List.of(submission)),
                anyMap(),
                anyMap()
        )).thenReturn(List.of(expected));

        List<PracticeSubmissionDetailsResponse> result =
                service.getLessonSubmissionsForTeacher(100L);

        assertEquals(List.of(expected), result);

        verify(practiceSubmissionResponseMapper).toSubmissionResponses(
                eq(List.of(submission)),
                eq(Map.of(10L, List.of(answer))),
                eq(Map.of(1000L, List.of(comment)))
        );
    }

    @Test
    void getLessonSubmissionsForTeacher_shouldThrowForbidden_whenTeacherCannotViewSubmission() {
        PracticeSubmission submission = activeSubmission();

        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(courseApi.isPractice(100L)).thenReturn(true);
        when(practiceSubmissionRepository.findByLessonId(100L))
                .thenReturn(List.of(submission));
        when(enrollmentApi.isTeacherOfCourseEnrollment(2L, 200L))
                .thenReturn(false);

        assertThrows(ForbiddenException.class,
                () -> service.getLessonSubmissionsForTeacher(100L));

        verifyNoInteractions(practiceSubmissionAnswerRepository);
        verifyNoInteractions(practiceSubmissionCommentRepository);
    }

    private PracticeSubmission activeSubmission() {
        return PracticeSubmission.load(
                10L,
                100L,
                200L,
                1L,
                Instant.parse("2026-05-13T09:00:00Z"),
                null
        );
    }

    private PracticeSubmission checkedSubmission() {
        return PracticeSubmission.load(
                10L,
                100L,
                200L,
                1L,
                Instant.parse("2026-05-13T09:00:00Z"),
                Instant.parse("2026-05-13T09:30:00Z")
        );
    }

    private PracticeSubmissionAnswer answer() {
        return PracticeSubmissionAnswer.load(
                1000L,
                10L,
                "<p>Ответ</p>",
                50L,
                Instant.parse("2026-05-13T09:10:00Z")
        );
    }

    private PracticeSubmissionComment comment() {
        return PracticeSubmissionComment.load(
                5000L,
                1000L,
                "<p>Комментарий</p>",
                60L,
                Instant.parse("2026-05-13T09:20:00Z")
        );
    }
}