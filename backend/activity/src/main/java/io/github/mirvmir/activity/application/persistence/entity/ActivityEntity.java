package io.github.mirvmir.activity.application.persistence.entity;

import io.github.mirvmir.activity.domain.ActivityType;
import io.github.mirvmir.common.domain.ContentStatus;
import io.github.mirvmir.common.domain.ModerationStatus;
import io.github.mirvmir.common.domain.Money;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

import static lombok.AccessLevel.PROTECTED;

@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Getter
@Entity
@Table(name = "activity")
public class ActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(nullable = false)
    private String title;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "description_html")
    private String descriptionHtml;

    @Column(name = "max_bookable_seats", nullable = false)
    private Integer maxBookableSeats;

    @Embedded
    private Money price;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "moderation_comment")
    private String moderationComment;

    @Column(name = "booking_step_minutes")
    private Integer bookingStepMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", nullable = false)
    private ModerationStatus moderationStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_status", nullable = false)
    private ContentStatus contentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ActivityType type;

    @Column(name = "subject_id")
    private Long subjectId;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    private Set<ActivityTimeEntity> activityTimeEntities;

    @OneToMany(
            mappedBy = "activityEntity",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<ActivityTopicEntity> topicEntities = new HashSet<>();

    public void replaceTopics(Set<Long> topicIds) {
        this.topicEntities.clear();

        if (topicIds == null) {
            return;
        }

        topicIds.forEach(topicId ->
                this.topicEntities.add(new ActivityTopicEntity(this, topicId))
        );
    }
}
