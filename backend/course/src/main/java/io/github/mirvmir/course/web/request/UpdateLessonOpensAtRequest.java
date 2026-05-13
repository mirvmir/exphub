package io.github.mirvmir.course.web.request;

import java.time.Instant;

public record UpdateLessonOpensAtRequest(Instant opensAt) {
}