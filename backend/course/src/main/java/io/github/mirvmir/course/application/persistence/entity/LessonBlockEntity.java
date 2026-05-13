package io.github.mirvmir.course.application.persistence.entity;

import io.github.mirvmir.course.domain.content.LessonContentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Getter
@Entity
@Table(name = "lesson_block")
public class LessonBlockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_lesson_id", nullable = false)
    private CourseLessonEntity courseLesson;

    @Column(name = "stable_block_id", nullable = false)
    private UUID stableBlockId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private LessonContentType contentType;

    @Column(name = "content_hash", nullable = false)
    private String contentHash;

    @OneToOne(
            mappedBy = "lessonBlock",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private HtmlLessonEntity htmlLesson;

    @OneToOne(
            mappedBy = "lessonBlock",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private FileLessonEntity fileLesson;

    @OneToOne(
            mappedBy = "lessonBlock",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private VideoLessonEntity videoLesson;

    public void assignLesson(CourseLessonEntity courseLesson) {
        this.courseLesson = courseLesson;
    }

    public void assignContent() {
        if (htmlLesson != null) {
            htmlLesson.assignLessonBlock(this);
        }

        if (fileLesson != null) {
            fileLesson.assignLessonBlock(this);
        }

        if (videoLesson != null) {
            videoLesson.assignLessonBlock(this);
        }
    }
}