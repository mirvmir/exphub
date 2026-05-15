package io.github.mirvmir.profile.exception;

import io.github.mirvmir.common.exception.ErrorCode;

public enum ProfileErrorCode implements ErrorCode {
    MEDIA_FILE_NOT_FOUND("MEDIA_FILE_NOT_FOUND",
            "Media file not found"),
    PROFILE_NOT_FOUND("PROFILE_NOT_FOUND",
            "Profile not found");

    private final String code;
    private final String message;

    ProfileErrorCode(String code, String message) {
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
