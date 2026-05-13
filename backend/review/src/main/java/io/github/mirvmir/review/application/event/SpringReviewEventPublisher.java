package io.github.mirvmir.review.application.event;

import io.github.mirvmir.review.api.event.ReviewPublishedEvent;
import io.github.mirvmir.review.application.service.port.event.ReviewEventPublisher;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class SpringReviewEventPublisher implements ReviewEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(ReviewPublishedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}