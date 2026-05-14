package io.github.mirvmir.course.application.persistence.entity;

import io.github.mirvmir.common.domain.ContentStatus;
import io.github.mirvmir.course.domain.CourseLessonOpening;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static lombok.AccessLevel.PROTECTED;

@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Getter
@Entity
@Table(name = "course")
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_status", nullable = false)
    private ContentStatus status;

    @OneToMany(
            mappedBy = "courseEntity",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<CourseLessonOpeningEntity> lessonOpeningEntities = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "published_version", unique = true)
    private CourseVersionEntity publishedVersion;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "draft_version", unique = true, nullable = false)
    private CourseVersionEntity draftVersion;

    @Column(name = "subject_id")
    private Long subjectId;

    @OneToMany(
            mappedBy = "courseEntity",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<CourseTopicEntity> topicEntities = new HashSet<>();

    public void replaceTopics(Set<Long> topicIds) {
        this.topicEntities.clear();

        if (topicIds == null) {
            return;
        }

        topicIds.forEach(topicId ->
                this.topicEntities.add(new CourseTopicEntity(this, topicId))
        );
    }

    public void replaceLessonOpenings(Set<CourseLessonOpening> lessonOpenings) {
        this.lessonOpeningEntities.clear();

        if (lessonOpenings == null) {
            return;
        }

        lessonOpenings.forEach(opening ->
                this.lessonOpeningEntities.add(
                        new CourseLessonOpeningEntity(
                                this,
                                opening.getStableLessonId(),
                                opening.getOpensAt()
                        )
                )
        );
    }
}
