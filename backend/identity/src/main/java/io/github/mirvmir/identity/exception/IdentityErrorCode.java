package io.github.mirvmir.identity.exception;

import io.github.mirvmir.common.exception.ErrorCode;

public enum IdentityErrorCode implements ErrorCode {
    USER_NOT_FOUND("USER_NOT_FOUND",
            "Не удалось определить текущего пользователя"),
    UNAUTHORIZED("UNAUTHORIZED",
            "User is not authenticated");

    private final String code;
    private final String message;

    IdentityErrorCode(String code, String message) {
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
