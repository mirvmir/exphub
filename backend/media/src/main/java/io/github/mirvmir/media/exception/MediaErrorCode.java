package io.github.mirvmir.media.exception;

import io.github.mirvmir.common.exception.ErrorCode;

public enum MediaErrorCode implements ErrorCode {
    MEDIA_FILE_NOT_FOUND("MEDIA_FILE_NOT_FOUND",
            "Media file not found"),
    VIDEO_NOT_FOUND("VIDEO_NOT_FOUND",
            "Video not found");

    private final String code;
    private final String message;

    MediaErrorCode(String code, String message) {
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
