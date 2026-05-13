package io.github.mirvmir.course.application.persistence.entity;

import io.github.mirvmir.course.domain.LessonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Getter
@Entity
@Table(name = "course_lesson")
public class CourseLessonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_module_id", nullable = false)
    private CourseModuleEntity module;

    @Column(name = "stable_lesson_id", nullable = false)
    private UUID stableLessonId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LessonType type;

    @OneToMany(
            mappedBy = "courseLesson",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("sortOrder asc")
    private List<LessonBlockEntity> blocks = new ArrayList<>();

    public void assignModule(CourseModuleEntity module) {
        this.module = module;

        if (blocks != null) {
            for (LessonBlockEntity block : blocks) {
                block.assignLesson(this);
                block.assignContent();
            }
        }
    }
}