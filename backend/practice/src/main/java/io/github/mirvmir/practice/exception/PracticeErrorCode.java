package io.github.mirvmir.practice.exception;

import io.github.mirvmir.common.exception.ErrorCode;

public enum PracticeErrorCode implements ErrorCode {
    PRACTICE_SUBMISSION_NOT_FOUND("PRACTICE_SUBMISSION_NOT_FOUND",
            "PRACTICE_SUBMISSION_NOT_FOUND"),
    PRACTICE_SUBMISSION_ANSWER_NOT_FOUND("PRACTICE_SUBMISSION_ANSWER_NOT_FOUND",
            "PRACTICE_SUBMISSION_ANSWER_NOT_FOUND"),
    PRACTICE_SUBMISSION_ANSWER_FORBIDDEN("PRACTICE_SUBMISSION_ANSWER_FORBIDDEN",
            "PRACTICE_SUBMISSION_ANSWER_FORBIDDEN"),
    PRACTICE_SUBMISSION_COMMENT_FORBIDDEN("PRACTICE_SUBMISSION_COMMENT_FORBIDDEN",
                                   "PRACTICE_SUBMISSION_COMMENT_FORBIDDEN"),
    PRACTICE_SUBMISSION_CHECK_FORBIDDEN("PRACTICE_SUBMISSION_CHECK_FORBIDDEN",
            "PRACTICE_SUBMISSION_CHECK_FORBIDDEN"),
    IS_NOT_PRACTICE("IS_NOT_PRACTICE",
            "Lesson is not practice"),
    COURSE_ID("COURSE_ID",
            "Course not found");

    private final String code;
    private final String message;

    PracticeErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
