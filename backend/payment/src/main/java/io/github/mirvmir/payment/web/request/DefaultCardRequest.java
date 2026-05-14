package io.github.mirvmir.payment.web.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DefaultCardRequest(
        @NotNull
        @Positive
        Long cardId
) {
}
