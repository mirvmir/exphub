package io.github.mirvmir.enrollment.application.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Entity
@Table(
        name = "student_lesson_progress",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"enrollment_id", "course_lesson_id"}
        )
)
public class StudentLessonProgressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enrollment_id", nullable = false)
    private Long enrollmentId;

    @Column(name = "course_lesson_id", nullable = false)
    private Long courseLessonId;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;
}