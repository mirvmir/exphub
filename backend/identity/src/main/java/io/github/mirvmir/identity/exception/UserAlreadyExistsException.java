package io.github.mirvmir.identity.exception;

import lombok.Getter;

@Getter
public class UserAlreadyExistsException extends RuntimeException {
    private final String code;

    public UserAlreadyExistsException(String code, String message) {
        super(message);
        this.code = code;
    }
}
