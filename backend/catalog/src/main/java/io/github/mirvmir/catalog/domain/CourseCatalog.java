package io.github.mirvmir.catalog.domain;

import io.github.mirvmir.common.domain.Money;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class CourseCatalog {
    private Long id;
    @NonNull
    private Long courseId;
    @NonNull
    private String title;
    @NonNull
    private Long authorId;
    @NonNull
    private String authorName;
    @NonNull
    private String shortDescription;
    @NonNull
    private Money price;
    @NonNull
    private Double ratingAvg;
    @NonNull
    private Long reviewCount;
    @NonNull
    private Set<Long> topicIds;
    @NonNull
    private Set<Long> sectionIds;
    @NonNull
    private Set<Long> subjectIds;

    public static CourseCatalog create(Long courseId,
                                       String title,
                                       Long authorId,
                                       String authorName,
                                       String shortDescription,
                                       BigDecimal amount,
                                       Currency currency,
                                       Double ratingAvg,
                                       Long reviewCount,
                                       Set<Long> topicIds,
                                       Set<Long> sectionIds,
                                       Set<Long> subjectIds) {
        Money price = new Money(amount, currency);

        return new CourseCatalog(
                null,
                courseId,
                title,
                authorId,
                authorName,
                shortDescription,
                price,
                ratingAvg,
                reviewCount,
                topicIds == null ? new HashSet<>() : new HashSet<>(topicIds),
                sectionIds == null ? new HashSet<>() : new HashSet<>(sectionIds),
                subjectIds == null ? new HashSet<>() : new HashSet<>(subjectIds)
        );
    }

    public static CourseCatalog load(Long id,
                                     Long courseId,
                                     String title,
                                     Long authorId,
                                     String authorName,
                                     String shortDescription,
                                     BigDecimal amount,
                                     Currency currency,
                                     Double ratingAvg,
                                     Long reviewCount,
                                     Set<Long> topicIds,
                                     Set<Long> sectionIds,
                                     Set<Long> subjectIds) {
        Money price = new Money(amount, currency);

        return new CourseCatalog(
                id,
                courseId,
                title,
                authorId,
                authorName,
                shortDescription,
                price,
                ratingAvg,
                reviewCount,
                topicIds == null
                        ? new HashSet<>()
                        : new HashSet<>(topicIds),
                sectionIds == null
                        ? new HashSet<>()
                        : new HashSet<>(sectionIds),
                subjectIds == null
                        ? new HashSet<>()
                        : new HashSet<>(subjectIds)
        );
    }

    public void updateAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void updateContent(String title,
                              String shortDescription,
                              BigDecimal amount,
                              Currency currency,
                              Set<Long> topicIds,
                              Set<Long> sectionIds,
                              Set<Long> subjectIds) {
        this.title = title;
        this.shortDescription = shortDescription;
        this.price = new Money(amount, currency);
        this.topicIds = topicIds == null ? new HashSet<>() : new HashSet<>(topicIds);
        this.sectionIds = sectionIds == null ? new HashSet<>() : new HashSet<>(sectionIds);
        this.subjectIds = subjectIds == null ? new HashSet<>() : new HashSet<>(subjectIds);
    }

    public void updateRatingAvg(double ratingAvg) {
        this.ratingAvg = ratingAvg;
    }

    public void updateReviewCount(long reviewCount) {
        this.reviewCount = reviewCount;
    }

    protected void assignId(Long id) {
        this.id = id;
    }
}