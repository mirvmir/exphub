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
import io.github.mirvmir.practice.application.service.interfaces.PracticeService;
import io.github.mirvmir.practice.domain.PracticeSubmission;
import io.github.mirvmir.practice.domain.PracticeSubmissionAnswer;
import io.github.mirvmir.practice.domain.PracticeSubmissionComment;
import io.github.mirvmir.practice.exception.PracticeErrorCode;
import io.github.mirvmir.practice.web.request.CreatePracticeAnswerRequest;
import io.github.mirvmir.practice.web.request.CreatePracticeCommentRequest;
import io.github.mirvmir.practice.web.response.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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
    public PracticeAnswerResponse addAnswer(
            Long courseLessonId,
            CreatePracticeAnswerRequest request
    ) {
        Long studentId = identityApi.getCurrentUserId();
        Instant now = Instant.now(clock);

        boolean isPractice = courseApi.isPractice(courseLessonId);

        if (!isPractice) {
            throw new BusinessException(PracticeErrorCode.IS_NOT_PRACTICE);
        }

        PracticeSubmission submission =
                practiceSubmissionRepository.findByLessonIdAndStudentId(
                        courseLessonId,
                        studentId
                );

        if (submission == null) {
            StudentCourseEnrollmentResponse enrollment =
                    enrollmentApi.getStudentCourseEnrollment(
                            studentId,
                            courseLessonId
                    );

            if (enrollment == null) {
                throw new ForbiddenException(
                        PracticeErrorCode.PRACTICE_SUBMISSION_ANSWER_FORBIDDEN
                );
            }

            submission = PracticeSubmission.create(
                    courseLessonId,
                    enrollment.enrollmentId(),
                    studentId,
                    now
            );

            practiceSubmissionRepository.saveOrUpdate(submission);
        }

        PracticeSubmissionAnswer answer =
                submission.createAnswerByStudent(
                        request.html(),
                        request.fileId(),
                        now
                );

        PracticeSubmissionAnswer savedAnswer =
                practiceSubmissionAnswerRepository.saveOrUpdate(answer);

        return new PracticeAnswerResponse(
                savedAnswer.getId(),
                savedAnswer.getPracticeSubmissionId(),
                savedAnswer.getHtml(),
                savedAnswer.getFileId(),
                savedAnswer.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public PracticeCommentResponse addComment(Long answerId,
                                              CreatePracticeCommentRequest request) {
        Instant now = Instant.now(clock);

        PracticeSubmissionAnswer answer =
                practiceSubmissionAnswerRepository.findById(answerId);

        if (answer == null) {
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
            throw new NotFoundException(
                    PracticeErrorCode.PRACTICE_SUBMISSION_NOT_FOUND,
                    "Practice submission with id="
                            + answer.getPracticeSubmissionId()
                            + " not found"
            );
        }

        boolean teacherCanComment =
                enrollmentApi.isTeacherOfCourseEnrollment(
                        identityApi.getCurrentUserId(),
                        submission.getCourseEnrollmentId()
                );

        if (!teacherCanComment) {
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

        return new PracticeCommentResponse(
                savedComment.getId(),
                savedComment.getPracticeSubmissionAnswerId(),
                savedComment.getHtml(),
                savedComment.getFileId(),
                savedComment.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public PracticeSubmissionResponse checkSubmissionByTeacher(Long practiceSubmissionId) {
        Long teacherId = identityApi.getCurrentUserId();
        Instant now = Instant.now(clock);

        PracticeSubmission submission =
                practiceSubmissionRepository.findById(practiceSubmissionId);

        if (submission == null) {
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
            throw new ForbiddenException(
                    PracticeErrorCode.PRACTICE_SUBMISSION_CHECK_FORBIDDEN
            );
        }

        submission.checkByTeacher(now);

        PracticeSubmission savedSubmission =
                practiceSubmissionRepository.saveOrUpdate(submission);

        return new PracticeSubmissionResponse(
                savedSubmission.getId(),
                savedSubmission.getLessonId(),
                savedSubmission.getCourseEnrollmentId(),
                savedSubmission.getStudentId(),
                savedSubmission.getCreatedAt(),
                savedSubmission.getCheckedAt()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PracticeSubmissionDetailsResponse getMySubmission(Long courseLessonId) {
        Long studentId = identityApi.getCurrentUserId();

        StudentCourseEnrollmentResponse enrollment =
                enrollmentApi.getStudentCourseEnrollment(
                        studentId,
                        courseLessonId
                );

        if (enrollment == null) {
            throw new ForbiddenException(
                    PracticeErrorCode.PRACTICE_SUBMISSION_ANSWER_FORBIDDEN
            );
        }

        PracticeSubmission submission =
                practiceSubmissionRepository.findByLessonIdAndStudentId(
                        courseLessonId,
                        studentId
                );

        if (submission == null) {
            return new PracticeSubmissionDetailsResponse(
                    null,
                    courseLessonId,
                    enrollment.enrollmentId(),
                    studentId,
                    null,
                    null,
                    List.of()
            );
        }

        List<PracticeSubmissionAnswer> answers =
                practiceSubmissionAnswerRepository
                        .findByPracticeSubmissionId(submission.getId());

        List<Long> answerIds = answers.stream()
                .map(PracticeSubmissionAnswer::getId)
                .toList();

        Map<Long, List<PracticeSubmissionComment>> commentsByAnswerId =
                practiceSubmissionCommentRepository
                        .findByPracticeSubmissionAnswerIds(answerIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                PracticeSubmissionComment::getPracticeSubmissionAnswerId
                        ));

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

        return practiceSubmissionResponseMapper.toSubmissionResponse(
                submission,
                answerResponses
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PracticeSubmissionDetailsResponse> getLessonSubmissionsForTeacher(
            Long courseLessonId
    ) {
        Long teacherId = identityApi.getCurrentUserId();

        boolean isPractice = courseApi.isPractice(courseLessonId);

        if (!isPractice) {
            throw new BusinessException(PracticeErrorCode.IS_NOT_PRACTICE);
        }

        List<PracticeSubmission> submissions =
                practiceSubmissionRepository.findByLessonId(courseLessonId);

        if (submissions.isEmpty()) {
            return List.of();
        }

        for (PracticeSubmission submission : submissions) {
            boolean teacherCanView =
                    enrollmentApi.isTeacherOfCourseEnrollment(
                            teacherId,
                            submission.getCourseEnrollmentId()
                    );

            if (!teacherCanView) {
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
                practiceSubmissionCommentRepository
                        .findByPracticeSubmissionAnswerIds(answerIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                PracticeSubmissionComment::getPracticeSubmissionAnswerId
                        ));

        Map<Long, List<PracticeSubmissionAnswer>> answersBySubmissionId =
                answers.stream()
                        .collect(Collectors.groupingBy(
                                PracticeSubmissionAnswer::getPracticeSubmissionId
                        ));

        return practiceSubmissionResponseMapper.toSubmissionResponses(
                submissions,
                answersBySubmissionId,
                commentsByAnswerId
        );
    }
}
