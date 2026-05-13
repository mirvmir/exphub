package io.github.mirvmir.course.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CourseLessonOpening {

    private UUID stableLessonId;
    private Instant opensAt;

    public static CourseLessonOpening create(UUID stableLessonId,
                                             Instant opensAt) {
        return new CourseLessonOpening(stableLessonId, opensAt);
    }

    public void updateOpensAt(Instant opensAt) {
        this.opensAt = opensAt;
    }
}