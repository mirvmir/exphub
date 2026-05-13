package io.github.mirvmir.activity.application.service.port.event;

import io.github.mirvmir.activity.api.event.ActivityChangeTopicIds;
import io.github.mirvmir.activity.api.event.ActivityDeleteEvent;
import io.github.mirvmir.activity.api.event.ActivityPublishedEvent;

public interface ActivityEventPublisher {
    void publish(ActivityPublishedEvent event);
    void delete(ActivityDeleteEvent event);
    void changeTopic(ActivityChangeTopicIds event);
}
