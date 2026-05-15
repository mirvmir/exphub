package io.github.mirvmir.identity.web.handler;

import io.github.mirvmir.common.exception.ErrorResponse;
import io.github.mirvmir.common.exception.UnauthorizedException;
import io.github.mirvmir.identity.exception.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class IdentityExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUnauthorized(
            UnauthorizedException exception
    ) {
        return new ErrorResponse(
                exception.getCode(),
                exception.getMessage()
        );
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUnauthorized(
            UserAlreadyExistsException exception
    ) {
        return new ErrorResponse(
                exception.getCode(),
                exception.getMessage()
        );
    }

}
