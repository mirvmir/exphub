package io.github.mirvmir.taxonomy.exception;

import io.github.mirvmir.common.exception.ErrorCode;

public enum TaxonomyErrorCode implements ErrorCode {
    TOPIC_NOT_FOUND("TOPIC_NOT_FOUND",
            "Topic not found"),
    SECTION_NOT_FOUND("SECTION_NOT_FOUND",
            "Section not found"),
    TOPIC_SUGGESTION_NOT_ON_MODERATION("TOPIC_SUGGESTION_NOT_ON_MODERATION",
            "Approval is allowed only for topic on moderation"),
    TOPIC_SUGGESTION_COMMENT_REQUIRED("TOPIC_SUGGESTION_COMMENT_REQUIRED",
            "Moderation comment is required for rejection"),
    TOPIC_SUGGESTION_TOPIC_ID_REQUIRED("TOPIC_SUGGESTION_TOPIC_ID_REQUIRED",
            "Resolved topic ID is required for merge"),
    SUBJECT_NOT_FOUND("SUBJECT_NOT_FOUND",
            "Subject not found"),
    TOPIC_SUGGESTION_NOT_FOUND("TOPIC_SUGGESTION_NOT_FOUND",
            "Topic suggestion not found"),
    TOPIC_SUGGESTION_ALREADY_MODERATED("TOPIC_SUGGESTION_ALREADY_MODERATED",
            "Suggestion already moderated");

    private final String code;
    private final String message;

    TaxonomyErrorCode(String code, String message) {
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
