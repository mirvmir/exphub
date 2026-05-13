package io.github.mirvmir.course.application.persistence.entity;

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
@Table(name = "course_module")
public class CourseModuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_version_id", nullable = false)
    private CourseVersionEntity courseVersion;

    @Column(name = "stable_module_id", nullable = false)
    private UUID stableModuleId;

    @Column(nullable = false)
    private String title;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @OneToMany(
            mappedBy = "module",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("sortOrder asc")
    private List<CourseLessonEntity> lessons = new ArrayList<>();

    public void assignCourseVersion(CourseVersionEntity courseVersion) {
        this.courseVersion = courseVersion;

        if (lessons != null) {
            for (CourseLessonEntity lesson : lessons) {
                lesson.assignModule(this);
            }
        }
    }
}