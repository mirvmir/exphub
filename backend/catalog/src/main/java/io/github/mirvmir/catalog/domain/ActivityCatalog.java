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
public class ActivityCatalog {

    private Long id;
    @NonNull
    private Long activityId;
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
    private Format format;
    @NonNull
    private Set<Long> topicIds;
    @NonNull
    private Set<Long> sectionIds;
    @NonNull
    private Set<Long> subjectIds;

    public static ActivityCatalog create(Long activityId,
                                         String title,
                                         Long authorId,
                                         String authorName,
                                         String shortDescription,
                                         BigDecimal amount,
                                         Currency currency,
                                         Double ratingAvg,
                                         Long reviewCount,
                                         Format format,
                                         Set<Long> topicIds,
                                         Set<Long> sectionIds,
                                         Set<Long> subjectIds) {

        Money price = new Money(amount, currency);

        return new ActivityCatalog(
                null,
                activityId,
                title,
                authorId,
                authorName,
                shortDescription,
                price,
                ratingAvg,
                reviewCount,
                format,
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

    public static ActivityCatalog load(Long id,
                                       Long activityId,
                                       String title,
                                       Long authorId,
                                       String authorName,
                                       String shortDescription,
                                       BigDecimal amount,
                                       Currency currency,
                                       Double ratingAvg,
                                       Long reviewCount,
                                       Format format,
                                       Set<Long> topicIds,
                                       Set<Long> sectionIds,
                                       Set<Long> subjectIds) {

        Money price = new Money(amount, currency);

        return new ActivityCatalog(
                id,
                activityId,
                title,
                authorId,
                authorName,
                shortDescription,
                price,
                ratingAvg,
                reviewCount,
                format,
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
