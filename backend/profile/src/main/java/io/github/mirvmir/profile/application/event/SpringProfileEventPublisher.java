package io.github.mirvmir.profile.application.event;

import io.github.mirvmir.profile.api.event.ProfileCompletedEvent;
import io.github.mirvmir.profile.application.service.port.event.ProfileEventPublisher;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class SpringProfileEventPublisher implements ProfileEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void complete(ProfileCompletedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
