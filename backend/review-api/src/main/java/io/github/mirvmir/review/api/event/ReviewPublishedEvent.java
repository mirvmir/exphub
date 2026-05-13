package io.github.mirvmir.review.api.event;

public record ReviewPublishedEvent(
        Long activityId,
        Long courseId,
        Double score
) {
}
