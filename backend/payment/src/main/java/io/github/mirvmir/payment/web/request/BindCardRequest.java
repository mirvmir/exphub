package io.github.mirvmir.payment.web.request;

public record BindCardRequest(
        Long userId,
        String cardNumber,
        String cardHolder,
        String expiryMonth,
        String expiryYear,
        String cvc
) {
}