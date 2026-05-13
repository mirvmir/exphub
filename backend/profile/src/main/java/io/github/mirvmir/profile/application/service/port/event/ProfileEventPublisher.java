package io.github.mirvmir.profile.application.service.port.event;

import io.github.mirvmir.profile.api.event.ProfileCompletedEvent;

public interface ProfileEventPublisher {
    void complete(ProfileCompletedEvent event);
}
