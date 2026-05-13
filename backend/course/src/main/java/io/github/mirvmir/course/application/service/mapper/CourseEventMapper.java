package io.github.mirvmir.course.application.service.mapper;

import io.github.mirvmir.course.api.event.CoursePublishedEvent;
import io.github.mirvmir.course.domain.Course;
import org.springframework.stereotype.Component;

@Component
public class CourseEventMapper {

    public CoursePublishedEvent toPublishedEvent(Course course) {
        return new CoursePublishedEvent(
                course.getId(),
                course.getAuthorId(),
                course.getPublishedVersion().getTitle(),
                course.getPublishedVersion().getShortDescription(),
                course.getPublishedVersion().getPrice().getAmount(),
                course.getPublishedVersion().getPrice().getCurrency(),
                course.getTopicIds()
        );
    }
}