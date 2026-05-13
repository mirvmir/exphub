package io.github.mirvmir.review.application.service.port.event;

import io.github.mirvmir.review.api.event.ReviewPublishedEvent;

public interface ReviewEventPublisher {
    void publish(ReviewPublishedEvent event);
}
