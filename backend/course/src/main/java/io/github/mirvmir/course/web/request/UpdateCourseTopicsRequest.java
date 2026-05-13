package io.github.mirvmir.course.web.request;

import java.util.Set;

public record UpdateCourseTopicsRequest(Set<Long> topicIds) {
}