package io.github.mirvmir.profile.application.service.port.event;

import io.github.mirvmir.profile.api.event.ProfileCompletedEvent;
import io.github.mirvmir.profile.api.event.ProfileUpdated;

public interface ProfileEventPublisher {
    void complete(ProfileCompletedEvent event);
    void update(ProfileUpdated event);
}
