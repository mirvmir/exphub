package io.github.mirvmir.review.web.request;

public record CreateReviewRequest(
        String comment,
        Double score
) {
}