package io.github.mirvmir.payment.web.handler;

import io.github.mirvmir.payment.exception.CardBindingException;
import io.github.mirvmir.payment.exception.PaymentException;
import io.github.mirvmir.payment.exception.PaymentGatewayUnavailableException;
import io.github.mirvmir.payment.exception.PaymentUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PaymentExceptionHandler {

    @ExceptionHandler(PaymentUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handlePaymentUnavailable(
            PaymentUnavailableException exception
    ) {
        return new ErrorResponse(
                "PAYMENT_UNAVAILABLE",
                exception.getMessage()
        );
    }

    @ExceptionHandler(PaymentGatewayUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handlePaymentGatewayUnavailable(
            PaymentGatewayUnavailableException exception
    ) {
        return new ErrorResponse(
                "PAYMENT_GATEWAY_UNAVAILABLE",
                exception.getMessage()
        );
    }

    @ExceptionHandler(CardBindingException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleCardBindingException(
            CardBindingException exception
    ) {
        return new ErrorResponse(
                "CARD_BINDING_ERROR",
                exception.getMessage()
        );
    }

    @ExceptionHandler(PaymentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handlePaymentException(
            PaymentException exception
    ) {
        return new ErrorResponse(
                "PAYMENT_ERROR",
                exception.getMessage()
        );
    }

    public record ErrorResponse(
            String code,
            String message
    ) {
    }
}