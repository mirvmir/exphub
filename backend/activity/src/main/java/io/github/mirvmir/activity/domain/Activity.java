package io.github.mirvmir.activity.domain;

import io.github.mirvmir.activity.exception.ActivityErrorCode;
import io.github.mirvmir.common.domain.ContentStatus;
import io.github.mirvmir.common.domain.ModerationStatus;
import io.github.mirvmir.common.domain.Money;
import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class Activity {
    private Long id;
    @NonNull
    private Long authorId ;
    @NonNull
    private String title;
    private String shortDescription;
    private String descriptionHtml;
    @NonNull
    private Integer maxBookableSeats;
    @NonNull
    private Money price;
    @NonNull
    private Integer durationMinutes;
    private Long subjectId;
    @NonNull
    private ActivityType type;
    private Integer bookingStepMinutes;
    @NonNull
    private ContentStatus contentStatus;
    @NonNull
    private ModerationStatus moderationStatus;
    private String moderationComment;
    private Set<Long> topicIds;
    private Set<ActivityTime> activityTimes;

    public static Activity createIndividual(Long authorId,
                                            String title,
                                            String shortDescription,
                                            String descriptionHtml,
                                            BigDecimal amount,
                                            Currency currency,
                                            Integer durationMinutes,
                                            Long subjectId,
                                            Integer bookingStepMinutes,
                                            Set<Long> topicIds) {
        Money price = new Money(amount, currency);
        return new Activity(
                null,
                authorId,
                title,
                shortDescription,
                descriptionHtml,
                1,
                price,
                durationMinutes,
                subjectId,
                ActivityType.INDIVIDUAL,
                bookingStepMinutes,
                ContentStatus.DRAFT,
                ModerationStatus.DRAFT,
                null,
                topicIds,
                new HashSet<>()
        );
    }

    public static Activity createGroup(Long authorId,
                                       String title,
                                       String shortDescription,
                                       String descriptionHtml,
                                       Integer maxBookableSeats,
                                       BigDecimal amount,
                                       Currency currency,
                                       Integer durationMinutes,
                                       Long subjectId,
                                       Set<Long> topicIds) {
        if (maxBookableSeats == null || maxBookableSeats < 2) {
            throw new BusinessException(ActivityErrorCode.GROUP_CAPACITY_INVALID);
        }

        Money price = new Money(amount, currency);
        return new Activity(
                null,
                authorId,
                title,
                shortDescription,
                descriptionHtml,
                maxBookableSeats,
                price,
                durationMinutes,
                subjectId,
                ActivityType.GROUP,
                null,
                ContentStatus.DRAFT,
                ModerationStatus.DRAFT,
                null,
                topicIds,
                new HashSet<>()
        );
    }

    public static Activity load(Long id,
                                Long authorId,
                                String title,
                                String shortDescription,
                                String descriptionHtml,
                                Integer maxBookableSeats,
                                BigDecimal amount,
                                Currency currency,
                                Integer durationMinutes,
                                Long subjectId,
                                ActivityType type,
                                Integer bookingStepMinutes,
                                ContentStatus contentStatus,
                                ModerationStatus moderationStatus,
                                String moderationComment,
                                Set<Long> topicIds,
                                Set<ActivityTime> activityTimes) {
        Money price = new Money(amount, currency);

        return new Activity(
                id,
                authorId,
                title,
                shortDescription,
                descriptionHtml,
                maxBookableSeats,
                price,
                durationMinutes,
                subjectId,
                type,
                bookingStepMinutes,
                contentStatus,
                moderationStatus,
                moderationComment,
                topicIds,
                activityTimes
        );
    }

    public void edit(String title,
                     String shortDescription,
                     String descriptionHtml,
                     Integer maxBookableSeats,
                     BigDecimal amount,
                     Currency currency,
                     Integer durationMinutes,
                     Long subjectId,
                     Set<Long> topicIds) {
        if (ContentStatus.DRAFT != this.contentStatus
                || !(ModerationStatus.DRAFT == this.moderationStatus
                || ModerationStatus.REJECTED == this.moderationStatus)) {
            throw new BusinessException(ActivityErrorCode.ACTIVITY_NOT_DRAFT);
        }

        this.title = title;
        this.shortDescription = shortDescription;
        this.descriptionHtml = descriptionHtml;
        this.maxBookableSeats = maxBookableSeats;
        this.price = new Money(amount, currency);
        this.durationMinutes = durationMinutes;
        this.subjectId = subjectId;
        this.topicIds = topicIds == null
                ? new HashSet<>()
                : new HashSet<>(topicIds);

        this.moderationStatus = ModerationStatus.DRAFT;
    }

    public void requestPublication() {
        if (ContentStatus.DRAFT == contentStatus
                && ModerationStatus.REJECTED == moderationStatus) {
            throw new BusinessException(ActivityErrorCode.ACTIVITY_MUST_BE_EDITED_AFTER_REJECTION);
        }

        ensureState(
                ContentStatus.DRAFT,
                ModerationStatus.DRAFT,
                "Запрос на публикацию возможен только из черновика"
        );

        moderationStatus = ModerationStatus.PENDING;
        moderationComment = null;
    }

    public void approveModeration() {
        ensureState(
                ContentStatus.DRAFT,
                ModerationStatus.PENDING,
                "Одобрение возможно только для занятия на модерации"
        );

        moderationStatus = ModerationStatus.APPROVED;
        contentStatus = ContentStatus.ACTIVE;
        moderationComment = null;
    }

    public void rejectModeration(String moderationComment) {
        if ((moderationComment == null) || moderationComment.isBlank()) {
            throw new BusinessException(ActivityErrorCode.MODERATION_COMMENT_REQUIRED);
        }

        ensureState(
                ContentStatus.DRAFT,
                ModerationStatus.PENDING,
                "Отклонение возможно только для занятия на модерации"
        );

        moderationStatus = ModerationStatus.REJECTED;
        this.moderationComment = moderationComment;
    }

    public void archive() {
        ensureState(
                ContentStatus.ACTIVE,
                ModerationStatus.APPROVED,
                "Архивировать можно только опубликованное занятие"
        );

        this.contentStatus = ContentStatus.ARCHIVED;
    }

    public void unarchive() {
        ensureState(
                ContentStatus.ARCHIVED,
                ModerationStatus.APPROVED,
                "Разархивировать можно только архивированное занятие"
        );

        this.contentStatus = ContentStatus.ACTIVE;
    }

    public void block() {
        ensureState(
                ContentStatus.ACTIVE,
                ModerationStatus.APPROVED,
                "Заблокировать можно только опубликованное занятие"
        );

        this.contentStatus = ContentStatus.BLOCKED;
    }

    public void delete() {
        ensureNotDeleted();

        this.contentStatus = ContentStatus.DELETED;
    }

    public void updateTopics(Set<Long> topicIds) {
        ensureNotBlocked();
        ensureNotDeleted();

        this.topicIds = topicIds == null
                ? new HashSet<>()
                : new HashSet<>(topicIds);
    }

    public ActivitySlot reserveTimeByStudent(Instant now,
                                             Long userId,
                                             Long activityTimeId,
                                             Instant startAt) {
        ensureActive();
        ensureIndividual();

        if (startAt.isBefore(now)) {
            throw new BusinessException(ActivityErrorCode.ACTIVITY_TIME_IN_PAST);
        }

        ActivityTime time = findActivityTime(activityTimeId);

        long minutesFromWindowStart = Duration
                .between(time.getStartAt(), startAt)
                .toMinutes();

        if (minutesFromWindowStart < 0
                || minutesFromWindowStart % bookingStepMinutes != 0) {
            throw new BusinessException(ActivityErrorCode.ACTIVITY_NOT_IN_TIME);
        }

        Instant endAt = startAt.plus(durationMinutes, ChronoUnit.MINUTES);

        if (endAt.isAfter(time.getEndAt())) {
            throw new BusinessException(ActivityErrorCode.ACTIVITY_NOT_IN_TIME);
        }

        return ActivitySlot.create(
                this.id,
                this.authorId,
                userId,
                startAt,
                endAt,
                now
        );
    }

    public ActivitySlot createSlotByTeacher(Instant now,
                                            Instant startAt) {
        ensureActive();
        ensureGroup();

        if (startAt.isBefore(now)) {
            throw new BusinessException(ActivityErrorCode.ACTIVITY_TIME_IN_PAST);
        }

        Instant endAt = startAt.plus(durationMinutes, ChronoUnit.MINUTES);
        return ActivitySlot.create(
                this.id,
                this.authorId,
                this.authorId,
                startAt,
                endAt,
                now
        );
    }

    public ActivityTime createAvailabilityTime(Instant now,
                                               Instant startAt) {
        ensureActive();
        ensureIndividual();

        if (startAt.isBefore(now)) {
            throw new BusinessException(ActivityErrorCode.ACTIVITY_TIME_IN_PAST);
        }

        Instant endAt = startAt.plus(durationMinutes, ChronoUnit.MINUTES);
        ActivityTime availableTime = ActivityTime.create(
                startAt,
                endAt
        );
        activityTimes.add(availableTime);
        return availableTime;
    }

    public boolean isGroup() {
        return ActivityType.GROUP == this.type;
    }

    public boolean isIndividual() {
        return ActivityType.INDIVIDUAL == this.type;
    }

    public boolean isActive() {
        return ContentStatus.ACTIVE == this.contentStatus;
    }

    public boolean isArchive() {
        return ContentStatus.ARCHIVED == this.contentStatus;
    }

    public boolean isEditable() {
        return ContentStatus.DRAFT == this.contentStatus
                && !(ModerationStatus.PENDING == this.moderationStatus);
    }

    public boolean canRequestPublication() {
        return ContentStatus.DRAFT == this.contentStatus
                && ModerationStatus.DRAFT == this.moderationStatus;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public ActivityTime findActivityTime(Long activityTimeId) {
        return this.activityTimes.stream()
                .filter(m -> m.getId().equals(activityTimeId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(ActivityErrorCode.TIME_NOT_FOUND));
    }

    private void ensureActive() {
        if (contentStatus != ContentStatus.ACTIVE) {
            throw new BusinessException(ActivityErrorCode.ACTIVITY_NOT_ACTIVE);
        }
    }

    private void ensureState(ContentStatus expectedContentStatus,
                             ModerationStatus expectedModerationStatus,
                             String message) {
        if (this.contentStatus != expectedContentStatus
                || this.moderationStatus != expectedModerationStatus) {
            throw new IllegalStateException(message);
        }
    }

    private void ensureIndividual() {
        if (ActivityType.INDIVIDUAL != this.type) {
            throw new BusinessException(ActivityErrorCode.ONLY_FOR_INDIVIDUAL);
        }
    }

    private void ensureGroup() {
        if (ActivityType.GROUP != this.type) {
            throw new BusinessException(ActivityErrorCode.ONLY_FOR_GROUP);
        }
    }

    private void ensureNotDeleted() {
        if (ContentStatus.DELETED == this.contentStatus) {
            throw new BusinessException(ActivityErrorCode.ACTIVITY_DELETED);
        }
    }

    private void ensureNotBlocked() {
        if (ContentStatus.BLOCKED != this.contentStatus) {
            throw new BusinessException(ActivityErrorCode.ACTIVITY_BLOCKED);
        }
    }
}
