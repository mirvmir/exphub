package io.github.mirvmir.enrollment.application.persistence.entity;

import io.github.mirvmir.enrollment.domain.ActivityEnrollmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static lombok.AccessLevel.PROTECTED;

@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Getter
@Entity
@Table(name = "activity_enrollment")
public class ActivityEnrollmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "activity_slot_id", nullable = false)
    private Long activitySlotId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column
    private ActivityEnrollmentStatus status;

    @Column(name = "subscribed_at", nullable = false)
    private Instant subscribedAt;
}