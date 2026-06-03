package io.github.mirvmir.catalog.application.persistence.entity;

import io.github.mirvmir.common.domain.Money;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Set;

import static lombok.AccessLevel.PROTECTED;

@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Getter
@Entity
@Table(name = "course_catalog")
public class CourseCatalogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private String title;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "author_name", nullable = false)
    private String authorName;

    @Column(name = "short_description", nullable = false)
    private String shortDescription;

    @Embedded
    private Money price;

    @Column(name = "rating_avg", nullable = false)
    private Double ratingAvg;

    @Column(name = "review_count", nullable = false)
    private Long reviewCount;

    @ElementCollection
    @CollectionTable(
            name = "course_catalog_topic",
            joinColumns = @JoinColumn(name = "course_catalog_id")
    )
    @Column(name = "topic_id")
    private Set<Long> topicIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "course_catalog_section",
            joinColumns = @JoinColumn(name = "course_catalog_id")
    )
    @Column(name = "section_id")
    private Set<Long> sectionIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "course_catalog_subject",
            joinColumns = @JoinColumn(name = "course_catalog_id")
    )
    @Column(name = "subject_id")
    private Set<Long> subjectIds = new HashSet<>();
}