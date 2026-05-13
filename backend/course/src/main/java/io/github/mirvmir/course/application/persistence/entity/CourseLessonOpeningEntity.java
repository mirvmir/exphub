package io.github.mirvmir.course.application.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Getter
@Entity
@Table(name = "course_lesson_opening")
@IdClass(CourseLessonOpeningId.class)
public class CourseLessonOpeningEntity {

    @Id
    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Id
    @Column(name = "stable_lesson_id", nullable = false)
    private UUID stableLessonId;

    @Column(name = "opens_at", nullable = false)
    private Instant opensAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", insertable = false, updatable = false, nullable = false)
    private CourseEntity courseEntity;

    public CourseLessonOpeningEntity(CourseEntity courseEntity,
                                     UUID stableLessonId,
                                     Instant opensAt) {
        this.courseEntity = courseEntity;
        this.courseId = courseEntity.getId();
        this.stableLessonId = stableLessonId;
        this.opensAt = opensAt;
    }
}