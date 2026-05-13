package io.github.mirvmir.review.api.dto;

public record ReviewRatingInfoResponse(
        Double ratingAvg,
        Long reviewCount
) {
}