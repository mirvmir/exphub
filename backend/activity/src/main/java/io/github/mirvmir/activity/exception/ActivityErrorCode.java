package io.github.mirvmir.activity.exception;

import io.github.mirvmir.common.exception.ErrorCode;

public enum ActivityErrorCode implements ErrorCode {
    ACTIVITY_MUST_BE_EDITED_AFTER_REJECTION("ACTIVITY_MUST_BE_EDITED_AFTER_REJECTION",
            "Activity must be edited after rejection before publication"),
    MODERATION_COMMENT_REQUIRED("MODERATION_COMMENT_REQUIRED",
            "Moderation comment is required for rejection"),
    ACTIVITY_NOT_ACTIVE("ACTIVITY_NOT_ACTIVE",
            "Activity cannot be edited in current state"),
    ACTIVITY_NOT_DRAFT("ACTIVITY_NOT_DRAFT",
            "Only draft activities can be modified"),
    GROUP_CAPACITY_INVALID("GROUP_CAPACITY_INVALID",
            "Group capacity must be at least 2"),
    ACTIVITY_TIME_IN_PAST("ACTIVITY_TIME_IN_PAST",
            "Activity time cannot be created in the past"),
    ACTIVITY_NOT_IN_TIME("ACTIVITY_NOT_IN_TIME",
            "Activity time does not match the allowed booking schedule"),
    ONLY_FOR_INDIVIDUAL("ONLY_FOR_INDIVIDUAL",
            "This action is available only for individual activities"),
    ONLY_FOR_GROUP("ONLY_FOR_GROUP",
            "This action is available only for group activities"),
    SLOT_CANCELLATION_FORBIDDEN("SLOT_CANCELLATION_FORBIDDEN",
            "Only the creator of an individual booking can cancel this slot."),
    TIME_NOT_FOUND("TIME_NOT_FOUND",
            "Requested activity time was not found"),
    ACTIVITY_NOT_FOUND("ACTIVITY_NOT_FOUND",
            "Activity not found"),
    ACTIVITY_HAS_PLANNED_SLOTS("ACTIVITY_HAS_PLANNED_SLOTS",
            "Activity has planned slots"),
    ACTIVITY_SLOT_TIME_CONFLICT("ACTIVITY_SLOT_TIME_CONFLICT",
            "Activity slot time conflicts with existing booking"),
    ACTIVITY_FORBIDDEN("ACTIVITY_FORBIDDEN",
            "You are not allowed to manage this activity"),
    ACTIVITY_TIME_ALREADY_BOOKED("ACTIVITY_TIME_ALREADY_BOOKED",
            "Activity in this time already booked"),
    ACTIVITY_SLOT_NOT_FOUND("ACTIVITY_SLOT_NOT_FOUND",
            "Activity slot not found"),
    ACTIVITY_SLOT_NOT_PLANNED("ACTIVITY_SLOT_NOT_PLANNED",
            "Activity slot is not planned"),
    ACTIVITY_SLOT_CANCELLATION_TOO_LATE("ACTIVITY_SLOT_CANCELLATION_TOO_LATE",
            "Activity slot cannot be cancelled less than configured hours before start"),
    ACTIVITY_SLOT_ALREADY_STARTED("ACTIVITY_SLOT_ALREADY_STARTED",
            "Room join url can be changed only before activity slot start"),
    ACTIVITY_DELETED("ACTIVITY_DELETED",
            "Activity was deleted"),
    ACTIVITY_BLOCKED("ACTIVITY_BLOCKED",
            "Activity was blocked"),
    TOPIC_SUBJECT_MISMATCH("TOPIC_SUBJECT_MISMATCH",
            "One or more topics do not belong to the specified subject"),
    ACTIVITY_DESCRIPTION_REQUIRED("ACTIVITY_DESCRIPTION_REQUIRED",
            "Activity description is required"),
    TOPIC_NOT_FOUND("TOPIC_NOT_FOUND",
            "One or more topics not found"),
    ACTIVITY_TIME_INVALID("ACTIVITY_TIME_INVALID",
            "Activity time invalid");

    private final String code;
    private final String message;

    ActivityErrorCode(String code, String message) {
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
