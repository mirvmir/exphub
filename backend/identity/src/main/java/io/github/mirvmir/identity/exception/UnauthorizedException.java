package io.github.mirvmir.identity.exception;

import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {

    private final String code;

    public UnauthorizedException(String code, String message) {
        super(message);
        this.code = code;
    }
}
