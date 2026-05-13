package io.github.mirvmir.activity.domain;

import io.github.mirvmir.activity.exception.ActivityErrorCode;
import io.github.mirvmir.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.time.Duration;
import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class ActivitySlot {
    private Long id;
    @NonNull
    private Long activityId;
    @NonNull
    private Long teacherId;
    @NonNull
    private Long createdByUserId;
    @NonNull
    private Instant startAt;
    @NonNull
    private Instant endAt;
    @NonNull
    private Instant createdAt;
    private String roomJoinUrl;
    @NonNull
    private ActivitySlotStatus status;

    protected static ActivitySlot create(Long activityId,
                                         Long teacherId,
                                         Long createdByUserId,
                                         Instant startAt,
                                         Instant endAt,
                                         Instant now) {
        return new ActivitySlot(
                null,
                activityId,
                teacherId,
                createdByUserId,
                startAt,
                endAt,
                now,
                null,
                ActivitySlotStatus.PLANNED
        );
    }

    public static ActivitySlot load(Long id,
                                    Long activityId,
                                    Long teacherId,
                                    Long createdByUserId,
                                    Instant startAt,
                                    Instant endAt,
                                    Instant createdAt,
                                    String roomJoinUrl,
                                    ActivitySlotStatus status) {
        return new ActivitySlot(
                id,
                activityId,
                teacherId,
                createdByUserId,
                startAt,
                endAt,
                createdAt,
                roomJoinUrl,
                status
        );
    }

    public void cancelByAuthor(Long authorId,
                               Instant now,
                               long minHoursBeforeStart) {
        ensureCanCancel(now, minHoursBeforeStart);

        this.status = ActivitySlotStatus.CANCELLED;
    }

    public void cancelByStudent(Long studentId,
                                Instant now,
                                long minHoursBeforeStart) {
        ensureCanCancel(now, minHoursBeforeStart);

        if (!this.createdByUserId.equals(studentId)) {
            throw new BusinessException(ActivityErrorCode.SLOT_CANCELLATION_FORBIDDEN);
        }

        this.status = ActivitySlotStatus.CANCELLED;
    }

    public void complete() {
        if (this.status != ActivitySlotStatus.PLANNED) {
            throw new BusinessException(ActivityErrorCode.ACTIVITY_SLOT_NOT_PLANNED);
        }

        this.status = ActivitySlotStatus.COMPLETED;
    }

    public void updateRoomJoinUrl(String roomJoinUrl,
                                  Instant now) {
        if (this.status != ActivitySlotStatus.PLANNED) {
            throw new BusinessException(ActivityErrorCode.ACTIVITY_SLOT_NOT_PLANNED);
        }

        if (!now.isBefore(this.startAt)) {
            throw new BusinessException(ActivityErrorCode.ACTIVITY_SLOT_ALREADY_STARTED);
        }

        this.roomJoinUrl = roomJoinUrl;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    private void ensureCanCancel(Instant now, long minHoursBeforeStart) {
        long hoursBeforeStart = Duration
                .between(now, this.startAt)
                .toHours();

        if (hoursBeforeStart < minHoursBeforeStart) {
            throw new BusinessException(
                    ActivityErrorCode.ACTIVITY_SLOT_CANCELLATION_TOO_LATE
            );
        }
    }
}
