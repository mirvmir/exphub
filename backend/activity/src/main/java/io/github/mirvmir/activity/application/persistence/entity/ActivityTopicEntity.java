package io.github.mirvmir.activity.application.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Getter
@Entity
@Table(name = "activity_topic")
public class ActivityTopicEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private ActivityEntity activityEntity;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    public ActivityTopicEntity(ActivityEntity activityEntity,
                               Long topicId) {
        this.activityEntity = activityEntity;
        this.topicId = topicId;
    }
}
