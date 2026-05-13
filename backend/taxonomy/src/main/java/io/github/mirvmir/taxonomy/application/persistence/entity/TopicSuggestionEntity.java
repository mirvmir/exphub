package io.github.mirvmir.taxonomy.application.persistence.entity;

import io.github.mirvmir.taxonomy.domain.SuggestionsStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Getter
@Entity
@Table(name = "topic_suggestion")
public class TopicSuggestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "section_id", nullable = false)
    private Long sectionId;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column
    private SuggestionsStatus status;

    @Column(name = "moderation_comment")
    private String moderationComment;

    @Column(name = "resolved_topic_id")
    private Long resolvedTopicId;
}
