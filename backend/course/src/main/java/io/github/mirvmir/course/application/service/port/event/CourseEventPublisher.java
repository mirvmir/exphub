package io.github.mirvmir.course.application.service.port.event;

import io.github.mirvmir.course.api.event.CourseChangeTopicIds;
import io.github.mirvmir.course.api.event.CourseDeleteEvent;
import io.github.mirvmir.course.api.event.CoursePublishedEvent;

public interface CourseEventPublisher {
    void publish(CoursePublishedEvent event);
    void delete(CourseDeleteEvent event);
    void changeTopic(CourseChangeTopicIds event);
}
