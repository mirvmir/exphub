package io.github.mirvmir.payment.web.response;

public record BindCardResponse(
        Long cardId,
        String maskedPan,
        String paymentSystem
) {
}
