package io.github.mirvmir.review.exception;

import io.github.mirvmir.common.exception.ErrorCode;

public enum ReviewErrorCode implements ErrorCode {
    REVIEW_NOT_FOUND("REVIEW_NOT_FOUND",
            "Review not found"),
    USER_IS_NOT_ACTIVITY_STUDENT("USER_IS_NOT_ACTIVITY_STUDENT",
            "Не удалось определить текущего пользователя"),
    USER_IS_NOT_COURSE_STUDENT("USER_IS_NOT_COURSE_STUDENT",
            "Пользователь не является студентом данного курса"),
    PUBLISHED_REVIEW_ALREADY_EXISTS("PUBLISHED_REVIEW_ALREADY_EXISTS",
            "У пользователя уже есть опубликованный отзыв"),
    REVIEW_ALREADY_EXISTS("REVIEW_ALREADY_EXISTS",
            "У пользователя уже есть отзыв");;

    private final String code;
    private final String message;

    ReviewErrorCode(String code, String message) {
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
