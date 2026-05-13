package io.github.mirvmir.enrollment.application.persistence.entity;

import io.github.mirvmir.enrollment.domain.CourseEnrollmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.math.BigDecimal;
import java.time.Instant;

import static lombok.AccessLevel.PROTECTED;

@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Getter
@Entity
@Table(name = "course_enrollment")
public class CourseEnrollmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "published_version_id", nullable = false)
    private Long publishedVersionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseEnrollmentStatus status;

    @Column(name = "progress_percent")
    private BigDecimal progressPercent;

    @Column(name = "subscribed_at", nullable = false)
    private Instant subscribedAt;
}
