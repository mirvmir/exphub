package io.github.mirvmir.review.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Review {
    private Long id;
    private String comment;
    @NonNull
    private Double score;
    @NonNull
    private Long toItemId;
    @NonNull
    private Long fromUserId;
    @NonNull
    private ReviewTargetType targetType;
    @NonNull
    private ReviewStatus status;

    public static Review create(String comment,
                                Double score,
                                Long toItemId,
                                Long fromUserId,
                                ReviewTargetType targetType) {
        return new Review(
                null,
                comment,
                score,
                toItemId,
                fromUserId,
                targetType,
                ReviewStatus.MODERATION
        );
    }

    public static Review load(Long id,
                              String comment,
                              Double score,
                              Long toItemId,
                              Long fromUserId,
                              ReviewTargetType targetType,
                              ReviewStatus status) {
        return new Review(
                id,
                comment,
                score,
                toItemId,
                fromUserId,
                targetType,
                status
        );
    }

    public void approve() {
        if (status != ReviewStatus.MODERATION) {
            throw new IllegalStateException("Можно подтвердить только отзыв на модерации");
        }

        this.status = ReviewStatus.PUBLISHED;
    }

    public void reject() {
        if (status != ReviewStatus.MODERATION) {
            throw new IllegalStateException("Можно отклонить только отзыв на модерации");
        }

        this.status = ReviewStatus.REJECTED;
    }

    public void edit(String comment,
                     Double score) {
        if (status == ReviewStatus.PUBLISHED) {
            throw new IllegalStateException(
                    "Нельзя редактировать опубликованный отзыв"
            );
        }

        this.comment = comment;
        this.score = score;
        this.status = ReviewStatus.MODERATION;
    }

    public boolean isRejected() {
        return ReviewStatus.REJECTED == this.status;
    }
}