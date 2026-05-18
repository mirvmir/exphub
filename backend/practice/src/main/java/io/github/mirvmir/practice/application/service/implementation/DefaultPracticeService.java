package io.github.mirvmir.practice.application.service.implementation;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.ForbiddenException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.common.exception.UnauthorizedException;
import io.github.mirvmir.course.api.CourseApi;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import io.github.mirvmir.enrollment.api.dto.StudentCourseEnrollmentResponse;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.practice.application.service.interfaces.PracticeService;
import io.github.mirvmir.practice.application.service.mapper.PracticeSubmissionResponseMapper;
import io.github.mirvmir.practice.application.service.port.repository.PracticeSubmissionAnswerRepository;
import io.github.mirvmir.practice.application.service.port.repository.PracticeSubmissionCommentRepository;
import io.github.mirvmir.practice.application.service.port.repository.PracticeSubmissionRepository;
import io.github.mirvmir.practice.domain.PracticeSubmission;
import io.github.mirvmir.practice.domain.PracticeSubmissionAnswer;
import io.github.mirvmir.practice.domain.PracticeSubmissionComment;
import io.github.mirvmir.practice.exception.PracticeErrorCode;
import io.github.mirvmir.practice.web.request.CreatePracticeAnswerRequest;
import io.github.mirvmir.practice.web.request.CreatePracticeCommentRequest;
import io.github.mirvmir.practice.web.response.PracticeAnswerDetailsResponse;
import io.github.mirvmir.practice.web.response.PracticeAnswerResponse;
import io.github.mirvmir.practice.web.response.PracticeCommentResponse;
import io.github.mirvmir.practice.web.response.PracticeSubmissionDetailsResponse;
import io.github.mirvmir.practice.web.response.PracticeSubmissionResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class DefaultPracticeService implements PracticeService {

    private final IdentityApi identityApi;
    private final EnrollmentApi enrollmentApi;
    private final CourseApi courseApi;

    private final PracticeSubmissionRepository practiceSubmissionRepository;
    private final PracticeSubmissionAnswerRepository practiceSubmissionAnswerRepository;
    private final PracticeSubmissionCommentRepository practiceSubmissionCommentRepository;

    private final PracticeSubmissionResponseMapper practiceSubmissionResponseMapper;

    private final Clock clock;

    @Override
    @Transactional
    public PracticeAnswerResponse addAnswer(UUID stableLessonId,
                                            CreatePracticeAnswerRequest request) {
        log.debug("Practice submission answer creation requested: stableLessonId={}",
                stableLessonId);

        Long studentId = identityApi.getCurrentUserId();
        Instant now = Instant.now(clock);

        if (studentId == null) {
            log.error("Unauthorized practice submission answer creation request");
            throw new UnauthorizedException(
                    "UNAUTHORIZED",
                    "User not authorized"
            );
        }

        boolean isPractice = courseApi.isPractice(stableLessonId);

        if (!isPractice) {
            log.warn("Practice lesson validation failed, lesson is not practice: stableLessonId={}, studentId={}",
                    stableLessonId, studentId);
            throw new BusinessException(PracticeErrorCode.IS_NOT_PRACTICE);
        }

        Long courseId = courseApi.getCourseIdByStableLessonId(stableLessonId);

        if (courseId == null) {
            log.error("Course loading by stable lesson id failed, course not found: stableLessonId={}, studentId={}",
                    stableLessonId, studentId);
            throw new NotFoundException(
                    PracticeErrorCode.COURSE_ID,
                    "Course by stableLessonId=" + stableLessonId + " not found"
            );
        }

        PracticeSubmission submission =
                practiceSubmissionRepository.findByStableLessonIdAndStudentId(
                        stableLessonId,
                        studentId
                );

        if (submission == null) {
            StudentCourseEnrollmentResponse enrollment =
                    enrollmentApi.getStudentCourseEnrollment(
                            studentId,
                            courseId
                    );

            if (enrollment == null) {
                log.warn("Practice submission answer creation forbidden, enrollment not found: stableLessonId={}, courseId={}, studentId={}",
                        stableLessonId, courseId, studentId);
                throw new ForbiddenException(
                        PracticeErrorCode.PRACTICE_SUBMISSION_ANSWER_FORBIDDEN
                );
            }

            submission = PracticeSubmission.create(
                    stableLessonId,
                    enrollment.enrollmentId(),
                    studentId,
                    now
            );

            PracticeSubmission savedSubmission =
                    practiceSubmissionRepository.saveOrUpdate(submission);

            submission = savedSubmission;

            log.info("Practice submission successfully created: stableLessonId={}, courseId={}, courseEnrollmentId={}, studentId={}",
                    stableLessonId, courseId, enrollment.enrollmentId(), studentId);
        }

        PracticeSubmissionAnswer answer =
                submission.createAnswerByStudent(
                        request.html(),
                        request.fileId(),
                        now
                );

        PracticeSubmissionAnswer savedAnswer =
                practiceSubmissionAnswerRepository.saveOrUpdate(answer);

        log.info("Practice submission answer successfully created: stableLessonId={}, courseId={}, practiceSubmissionId={}, answerId={}, studentId={}",
                stableLessonId, courseId, savedAnswer.getPracticeSubmissionId(), savedAnswer.getId(), studentId);

        return new PracticeAnswerResponse(
                savedAnswer.getId(),
                savedAnswer.getPracticeSubmissionId(),
                savedAnswer.getText(),
                savedAnswer.getFileId(),
                savedAnswer.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public PracticeCommentResponse addComment(Long answerId,
                                              CreatePracticeCommentRequest request) {
        log.debug("Practice submission comment creation requested: answerId={}",
                answerId);

        Long teacherId = identityApi.getCurrentUserId();
        Instant now = Instant.now(clock);

        if (teacherId == null) {
            log.error("Unauthorized practice submission comment creation request");
            throw new UnauthorizedException(
                    "UNAUTHORIZED",
                    "User not authorized"
            );
        }

        PracticeSubmissionAnswer answer =
                practiceSubmissionAnswerRepository.findById(answerId);

        if (answer == null) {
            log.error("Practice submission answer loading failed, answer not found: answerId={}, teacherId={}",
                    answerId, teacherId);
            throw new NotFoundException(
                    PracticeErrorCode.PRACTICE_SUBMISSION_ANSWER_NOT_FOUND,
                    "Practice answer with id=" + answerId + " not found"
            );
        }

        PracticeSubmission submission =
                practiceSubmissionRepository.findById(
                        answer.getPracticeSubmissionId()
                );

        if (submission == null) {
            log.error("Practice submission loading failed, submission not found: practiceSubmissionId={}, answerId={}, teacherId={}",
                    answer.getPracticeSubmissionId(), answerId, teacherId);
            throw new NotFoundException(
                    PracticeErrorCode.PRACTICE_SUBMISSION_NOT_FOUND,
                    "Practice submission with id="
                            + answer.getPracticeSubmissionId()
                            + " not found"
            );
        }

        boolean teacherCanComment =
                enrollmentApi.isTeacherOfCourseEnrollment(
                        teacherId,
                        submission.getCourseEnrollmentId()
                );

        if (!teacherCanComment) {
            log.warn("Practice submission comment creation forbidden: stableLessonId={}, courseEnrollmentId={}, practiceSubmissionId={}, answerId={}, teacherId={}",
                    submission.getStableLessonId(),
                    submission.getCourseEnrollmentId(),
                    submission.getId(),
                    answerId,
                    teacherId);
            throw new ForbiddenException(
                    PracticeErrorCode.PRACTICE_SUBMISSION_COMMENT_FORBIDDEN
            );
        }

        PracticeSubmissionComment comment =
                submission.createCommentByTeacher(
                        answer.getId(),
                        request.html(),
                        request.fileId(),
                        now
                );

        PracticeSubmissionComment savedComment =
                practiceSubmissionCommentRepository.saveOrUpdate(comment);

        log.info("Practice submission comment successfully created: stableLessonId={}, courseEnrollmentId={}, practiceSubmissionId={}, answerId={}, commentId={}, teacherId={}",
                submission.getStableLessonId(),
                submission.getCourseEnrollmentId(),
                submission.getId(),
                answerId,
                savedComment.getId(),
                teacherId);

        return new PracticeCommentResponse(
                savedComment.getId(),
                savedComment.getPracticeSubmissionAnswerId(),
                savedComment.getText(),
                savedComment.getFileId(),
                savedComment.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public PracticeSubmissionResponse checkSubmissionByTeacher(Long practiceSubmissionId) {
        log.debug("Practice submission check requested: practiceSubmissionId={}",
                practiceSubmissionId);

        Long teacherId = identityApi.getCurrentUserId();
        Instant now = Instant.now(clock);

        if (teacherId == null) {
            log.error("Unauthorized practice submission check request");
            throw new UnauthorizedException(
                    "UNAUTHORIZED",
                    "User not authorized"
            );
        }

        PracticeSubmission submission =
                practiceSubmissionRepository.findById(practiceSubmissionId);

        if (submission == null) {
            log.error("Practice submission loading failed, submission not found: practiceSubmissionId={}, teacherId={}",
                    practiceSubmissionId, teacherId);
            throw new NotFoundException(
                    PracticeErrorCode.PRACTICE_SUBMISSION_NOT_FOUND,
                    "Practice submission with id=" + practiceSubmissionId + " not found"
            );
        }

        boolean teacherCanCheck =
                enrollmentApi.isTeacherOfCourseEnrollment(
                        teacherId,
                        submission.getCourseEnrollmentId()
                );

        if (!teacherCanCheck) {
            log.warn("Practice submission check forbidden: stableLessonId={}, courseEnrollmentId={}, practiceSubmissionId={}, teacherId={}",
                    submission.getStableLessonId(),
                    submission.getCourseEnrollmentId(),
                    practiceSubmissionId,
                    teacherId);
            throw new ForbiddenException(
                    PracticeErrorCode.PRACTICE_SUBMISSION_CHECK_FORBIDDEN
            );
        }

        submission.checkByTeacher(now);

        PracticeSubmission savedSubmission =
                practiceSubmissionRepository.saveOrUpdate(submission);

        log.info("Practice submission successfully checked: stableLessonId={}, courseEnrollmentId={}, practiceSubmissionId={}, studentId={}, teacherId={}",
                savedSubmission.getStableLessonId(),
                savedSubmission.getCourseEnrollmentId(),
                savedSubmission.getId(),
                savedSubmission.getStudentId(),
                teacherId);

        return new PracticeSubmissionResponse(
                savedSubmission.getId(),
                savedSubmission.getStableLessonId(),
                savedSubmission.getCourseEnrollmentId(),
                savedSubmission.getStudentId(),
                savedSubmission.getCreatedAt(),
                savedSubmission.getCheckedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PracticeSubmissionDetailsResponse getMySubmission(UUID stableLessonId) {
        log.debug("Getting student practice submission: stableLessonId={}",
                stableLessonId);

        Long studentId = identityApi.getCurrentUserId();

        if (studentId == null) {
            log.error("Unauthorized student practice submission request");
            throw new UnauthorizedException(
                    "UNAUTHORIZED",
                    "User not authorized"
            );
        }

        boolean isPractice = courseApi.isPractice(stableLessonId);

        if (!isPractice) {
            log.warn("Practice lesson validation failed, lesson is not practice: stableLessonId={}, studentId={}",
                    stableLessonId,
                    studentId);
            throw new BusinessException(PracticeErrorCode.IS_NOT_PRACTICE);
        }

        Long courseId = courseApi.getCourseIdByStableLessonId(stableLessonId);

        if (courseId == null) {
            log.error("Course loading by stable lesson id failed, course not found: stableLessonId={}, studentId={}",
                    stableLessonId, studentId);
            throw new NotFoundException(
                    PracticeErrorCode.COURSE_ID,
                    "Course by stableLessonId=" + stableLessonId + " not found"
            );
        }

        StudentCourseEnrollmentResponse enrollment =
                enrollmentApi.getStudentCourseEnrollment(
                        studentId,
                        courseId
                );

        if (enrollment == null) {
            log.warn("Practice submission getting forbidden, enrollment not found: stableLessonId={}, courseId={}, studentId={}",
                    stableLessonId, courseId, studentId);
            throw new ForbiddenException(
                    PracticeErrorCode.PRACTICE_SUBMISSION_ANSWER_FORBIDDEN
            );
        }

        PracticeSubmission submission =
                practiceSubmissionRepository.findByStableLessonIdAndStudentId(
                        stableLessonId,
                        studentId
                );

        if (submission == null) {
            log.error("Student practice submission not found: stableLessonId={}, courseId={}, courseEnrollmentId={}, studentId={}",
                    stableLessonId, courseId, enrollment.enrollmentId(), studentId);
            throw new NotFoundException(
                    PracticeErrorCode.PRACTICE_SUBMISSION_NOT_FOUND,
                    "Practice submission by stableLessonId=" + stableLessonId + " not found"
            );
        }

        List<PracticeSubmissionAnswer> answers =
                practiceSubmissionAnswerRepository
                        .findByPracticeSubmissionId(submission.getId());

        List<Long> answerIds = answers.stream()
                .map(PracticeSubmissionAnswer::getId)
                .toList();

        Map<Long, List<PracticeSubmissionComment>> commentsByAnswerId =
                getCommentsByAnswerId(answerIds);

        List<PracticeAnswerDetailsResponse> answerResponses =
                answers.stream()
                        .map(answer -> practiceSubmissionResponseMapper.toAnswerResponse(
                                answer,
                                commentsByAnswerId.getOrDefault(
                                        answer.getId(),
                                        List.of()
                                )
                        ))
                        .toList();

        log.info("Student practice submission successfully received: stableLessonId={}, courseId={}, practiceSubmissionId={}, answersCount={}, studentId={}",
                stableLessonId,
                courseId,
                submission.getId(),
                answers.size(),
                studentId);

        return practiceSubmissionResponseMapper.toSubmissionResponse(
                submission,
                answerResponses
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PracticeSubmissionDetailsResponse> getLessonSubmissionsForTeacher(UUID stableLessonId) {
        log.debug("Getting lesson practice submissions for teacher: stableLessonId={}",
                stableLessonId);

        Long teacherId = identityApi.getCurrentUserId();

        if (teacherId == null) {
            log.error("Unauthorized teacher practice submissions request");
            throw new UnauthorizedException(
                    "UNAUTHORIZED",
                    "User not authorized"
            );
        }

        boolean isPractice = courseApi.isPractice(stableLessonId);

        if (!isPractice) {
            log.warn("Practice lesson validation failed, lesson is not practice: stableLessonId={}, teacherId={}",
                    stableLessonId, teacherId);
            throw new BusinessException(PracticeErrorCode.IS_NOT_PRACTICE);
        }

        Long courseId = courseApi.getCourseIdByStableLessonId(stableLessonId);

        if (courseId == null) {
            log.error("Course loading by stable lesson id failed, course not found: stableLessonId={}, teacherId={}",
                    stableLessonId, teacherId);
            throw new NotFoundException(
                    PracticeErrorCode.COURSE_ID,
                    "Course by stableLessonId=" + stableLessonId + " not found"
            );
        }

        List<PracticeSubmission> submissions =
                practiceSubmissionRepository.findByLessonId(stableLessonId);

        if (submissions.isEmpty()) {
            log.info("Lesson practice submissions successfully received: stableLessonId={}, courseId={}, submissionsCount={}, answersCount={}, teacherId={}",
                    stableLessonId, courseId, 0, 0, teacherId);
            return List.of();
        }

        for (PracticeSubmission submission : submissions) {
            boolean teacherCanView =
                    enrollmentApi.isTeacherOfCourseEnrollment(
                            teacherId,
                            submission.getCourseEnrollmentId()
                    );

            if (!teacherCanView) {
                log.warn("Practice submission getting forbidden: stableLessonId={}, courseId={}, courseEnrollmentId={}, practiceSubmissionId={}, teacherId={}",
                        stableLessonId,
                        courseId,
                        submission.getCourseEnrollmentId(),
                        submission.getId(),
                        teacherId);
                throw new ForbiddenException(
                        PracticeErrorCode.PRACTICE_SUBMISSION_CHECK_FORBIDDEN
                );
            }
        }

        List<Long> submissionIds = submissions.stream()
                .map(PracticeSubmission::getId)
                .toList();

        List<PracticeSubmissionAnswer> answers =
                practiceSubmissionAnswerRepository
                        .findByPracticeSubmissionIds(submissionIds);

        List<Long> answerIds = answers.stream()
                .map(PracticeSubmissionAnswer::getId)
                .toList();

        Map<Long, List<PracticeSubmissionComment>> commentsByAnswerId =
                getCommentsByAnswerId(answerIds);

        Map<Long, List<PracticeSubmissionAnswer>> answersBySubmissionId =
                answers.stream()
                        .collect(Collectors.groupingBy(
                                PracticeSubmissionAnswer::getPracticeSubmissionId
                        ));

        log.info("Lesson practice submissions successfully received: stableLessonId={}, courseId={}, submissionsCount={}, answersCount={}, teacherId={}",
                stableLessonId,
                courseId,
                submissions.size(),
                answers.size(),
                teacherId);

        return practiceSubmissionResponseMapper.toSubmissionResponses(
                submissions,
                answersBySubmissionId,
                commentsByAnswerId
        );
    }

    private Map<Long, List<PracticeSubmissionComment>> getCommentsByAnswerId(List<Long> answerIds) {
        if (answerIds.isEmpty()) {
            return Map.of();
        }

        return practiceSubmissionCommentRepository
                .findByPracticeSubmissionAnswerIds(answerIds)
                .stream()
                .collect(Collectors.groupingBy(
                        PracticeSubmissionComment::getPracticeSubmissionAnswerId
                ));
    }
}