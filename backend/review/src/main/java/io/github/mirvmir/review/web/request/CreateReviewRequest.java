package io.github.mirvmir.review.web.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewRequest(
        @Size(max = 255)
        String comment,
        @NotNull
        @DecimalMin(value = "1.0")
        @DecimalMax(value = "5.0")
        Double score
) {
}