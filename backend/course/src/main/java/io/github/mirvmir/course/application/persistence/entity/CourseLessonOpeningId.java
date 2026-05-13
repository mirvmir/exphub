package io.github.mirvmir.course.application.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
public class CourseLessonOpeningId implements Serializable {
    private Long courseId;
    private UUID stableLessonId;
}