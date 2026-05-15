package io.github.mirvmir.course.exception;

import io.github.mirvmir.common.exception.ErrorCode;

public enum CourseErrorCode implements ErrorCode {
    COURSE_NOT_FOUND("COURSE_NOT_FOUND",
            "Course not found"),
    COURSE_FORBIDDEN("COURSE_FORBIDDEN",
            "You are not allowed to manage this course"),
    COURSE_TITLE_REQUIRED("COURSE_TITLE_REQUIRED",
            "Course title is required"),
    COURSE_DESCRIPTION_REQUIRED("COURSE_DESCRIPTION_REQUIRED",
            "Course description is required"),
    COURSE_MODULES_REQUIRED("COURSE_MODULES_REQUIRED",
            "Course must contain at least one module"),
    COURSE_HAS_NO_PUBLISHED_VERSION("COURSE_HAS_NO_PUBLISHED_VERSION",
            "Course has no published version"),
    COURSE_DRAFT_VERSION_NOT_FOUND("COURSE_DRAFT_VERSION_NOT_FOUND",
            "Course draft version not found"),
    COURSE_VERSION_NOT_EDITABLE("COURSE_VERSION_NOT_EDITABLE",
            "Course version cannot be edited in current state"),
    COURSE_VERSION_NOT_ON_MODERATION("COURSE_VERSION_NOT_ON_MODERATION",
            "Course version is not on moderation"),
    COURSE_DELETED("COURSE_DELETED",
            "Course is deleted"),
    COURSE_BLOCKED("COURSE_BLOCKED",
            "Course is blocked"),
    COURSE_MODULE_NOT_FOUND("COURSE_MODULE_NOT_FOUND",
            "Course module not found"),
    COURSE_MODULE_TITLE_REQUIRED("COURSE_MODULE_TITLE_REQUIRED",
            "Course module title is required"),
    COURSE_MODULE_LESSONS_REQUIRED("COURSE_MODULE_LESSONS_REQUIRED",
            "Course module must contain at least one lesson"),
    COURSE_LESSON_NOT_FOUND("COURSE_LESSON_NOT_FOUND",
            "Course lesson not found"),
    COURSE_LESSON_TITLE_REQUIRED("COURSE_LESSON_TITLE_REQUIRED",
            "Course lesson title is required"),
    COURSE_LESSON_BLOCKS_REQUIRED("COURSE_LESSON_BLOCKS_REQUIRED",
            "Course lesson must contain at least one content block"),
    LESSON_BLOCK_NOT_FOUND("LESSON_BLOCK_NOT_FOUND",
            "Lesson block not found"),
    LESSON_BLOCK_TYPE_REQUIRED("LESSON_BLOCK_TYPE_REQUIRED",
            "Lesson block type is required"),
    LESSON_BLOCK_HTML_REQUIRED("LESSON_BLOCK_HTML_REQUIRED",
            "HTML content is required"),
    LESSON_BLOCK_FILE_REQUIRED("LESSON_BLOCK_FILE_REQUIRED",
            "File asset id is required"),
    LESSON_BLOCK_VIDEO_REQUIRED("LESSON_BLOCK_VIDEO_REQUIRED",
            "Video asset id is required"),
    MODERATION_COMMENT_REQUIRED("MODERATION_COMMENT_REQUIRED",
            "Moderation comment is required"),
    STABLE_LESSON_ID_REQUIRED("STABLE_LESSON_ID_REQUIRED",
            "Stable lesson id is required"),
    COURSE_LESSON_NOT_OPENED("COURSE_LESSON_NOT_OPENED",
            "Course lesson is closed"),
    COURSE_ENROLLMENT_NOT_FOUND("COURSE_ENROLLMENT_NOT_FOUND",
            "COURSE_ENROLLMENT_NOT_FOUND"),
    COURSE_ENROLLMENT_VERSION_NOT_FOUND("COURSE_ENROLLMENT_VERSION_NOT_FOUND",
            "Course version is not assigned to enrollment"),
    COURSE_VERSION_NOT_FOUND("COURSE_VERSION_NOT_FOUND",
            "Course version not found"),
    TOPIC_SUBJECT_MISMATCH("TOPIC_SUBJECT_MISMATCH",
            "One or more topics do not belong to the specified subject"),
    INVALID_LESSON_BLOCK_HTML("INVALID_LESSON_BLOCK_HTML",
            "");

    private final String code;
    private final String message;

    CourseErrorCode(String code, String message) {
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