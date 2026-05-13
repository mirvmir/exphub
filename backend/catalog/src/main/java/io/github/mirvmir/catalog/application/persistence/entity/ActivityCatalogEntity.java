package io.github.mirvmir.catalog.application.persistence.entity;

import io.github.mirvmir.catalog.domain.Format;
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
@Table(name = "activity_catalog")
public class ActivityCatalogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column
    private String title;

    @Column(name = "author_name")
    private String authorName;

    @Column(nullable = false)
    private String shortDescription;

    @Embedded
    private Money price;

    @Column(name = "rating_avg", nullable = false)
    private Double ratingAvg;

    @Column(name = "review_count", nullable = false)
    private Long reviewCount;

    @Enumerated(EnumType.STRING)
    @Column
    private Format format;

    @ElementCollection
    @CollectionTable(
            name = "activity_catalog_topic",
            joinColumns = @JoinColumn(name = "activity_catalog_id")
    )
    @Column(name = "topic_id")
    private Set<Long> topicIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "activity_catalog_section",
            joinColumns = @JoinColumn(name = "activity_catalog_id")
    )
    @Column(name = "section_id")
    private Set<Long> sectionIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "activity_catalog_subject",
            joinColumns = @JoinColumn(name = "activity_catalog_id")
    )
    @Column(name = "subject_id")
    private Set<Long> subjectIds = new HashSet<>();
}