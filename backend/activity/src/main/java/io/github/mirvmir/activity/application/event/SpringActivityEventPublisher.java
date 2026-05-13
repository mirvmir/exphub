package io.github.mirvmir.activity.application.event;

import io.github.mirvmir.activity.api.event.ActivityChangeTopicIds;
import io.github.mirvmir.activity.api.event.ActivityDeleteEvent;
import io.github.mirvmir.activity.api.event.ActivityPublishedEvent;
import io.github.mirvmir.activity.application.service.port.event.ActivityEventPublisher;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class SpringActivityEventPublisher implements ActivityEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(ActivityPublishedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void delete(ActivityDeleteEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void changeTopic(ActivityChangeTopicIds event) {
        applicationEventPublisher.publishEvent(event);
    }
}