package io.github.mirvmir.common.exception;

public record ErrorResponse(
        String code,
        String message
) {
}