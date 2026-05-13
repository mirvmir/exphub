package io.github.mirvmir.practice.domain;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.practice.exception.PracticeErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class PracticeSubmission {
    private Long id;
    @NonNull
    private Long lessonId;
    @NonNull
    private Long courseEnrollmentId;
    @NonNull
    private Long studentId;
    @NonNull
    private Instant createdAt;
    private Instant checkedAt;

    public static PracticeSubmission create(
            Long lessonId,
            Long courseEnrollmentId,
            Long studentId,
            Instant now
    ) {
        return new PracticeSubmission(
                null,
                lessonId,
                courseEnrollmentId,
                studentId,
                now,
                null
        );
    }

    public static PracticeSubmission load(
            Long id,
            Long lessonId,
            Long courseEnrollmentId,
            Long studentId,
            Instant createdAt,
            Instant checkedAt
    ) {
        return new PracticeSubmission(
                id,
                lessonId,
                courseEnrollmentId,
                studentId,
                createdAt,
                checkedAt
        );
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public void checkByTeacher(Instant now) {
        this.checkedAt = now;
    }

    public PracticeSubmissionAnswer createAnswerByStudent(
            String html,
            Long fileId,
            Instant now
    ) {
        if (this.checkedAt != null) {
            throw new BusinessException(PracticeErrorCode.PRACTICE_SUBMISSION_ANSWER_FORBIDDEN);
        }

        return PracticeSubmissionAnswer.createByStudent(
                this.id,
                html,
                fileId,
                now
        );
    }

    public PracticeSubmissionComment createCommentByTeacher(
            Long answerId,
            String html,
            Long fileId,
            Instant now
    ) {
        if (this.checkedAt != null) {
            throw new BusinessException(PracticeErrorCode.PRACTICE_SUBMISSION_COMMENT_FORBIDDEN);
        }

        return PracticeSubmissionComment.createByTeacher(
                answerId,
                html,
                fileId,
                now
        );
    }
}