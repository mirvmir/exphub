package io.github.mirvmir.course.application.event;

import io.github.mirvmir.course.api.event.CourseChangeTopicIds;
import io.github.mirvmir.course.api.event.CourseDeleteEvent;
import io.github.mirvmir.course.api.event.CoursePublishedEvent;
import io.github.mirvmir.course.application.service.port.event.CourseEventPublisher;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class SpringCourseEventPublisher implements CourseEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(CoursePublishedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void delete(CourseDeleteEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void changeTopic(CourseChangeTopicIds event) {
        applicationEventPublisher.publishEvent(event);
    }
}
