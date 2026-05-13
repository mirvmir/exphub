package io.github.mirvmir.course.api.event;

import java.util.Set;

public record CourseChangeTopicIds(
        Long courseId,
        Set<Long> topicIds
) {
}
