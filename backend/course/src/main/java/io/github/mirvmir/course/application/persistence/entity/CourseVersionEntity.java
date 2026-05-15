package io.github.mirvmir.course.application.persistence.entity;

import io.github.mirvmir.common.domain.ModerationStatus;
import io.github.mirvmir.common.domain.Money;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Getter
@Entity
@Table(name = "course_version")
public class CourseVersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Embedded
    private Money price;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", nullable = false)
    private ModerationStatus moderationStatus;

    @Column(name = "moderation_comment")
    private String moderationComment;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "description_html")
    private String descriptionHtml;

    @OneToMany(
            mappedBy = "courseVersion",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("sortOrder asc")
    private List<CourseModuleEntity> modules = new ArrayList<>();
}
