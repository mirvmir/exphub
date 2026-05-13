package io.github.mirvmir.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class ActivityNotification {
    private Long id;
    @NonNull
    private Long activitySlotId;
    @NonNull
    private Instant notifyAt;
    private Instant sentAt;
    @NonNull
    private NotificationStatus status;

    public static ActivityNotification create(Long activitySlotId,
                                              Instant now) {
        return new ActivityNotification(
                null,
                activitySlotId,
                now,
                null,
                NotificationStatus.PENDING
        );
    }

    public static ActivityNotification load(Long id,
                                            Long activitySlotId,
                                            Instant notifyAt,
                                            Instant sentAt,
                                            NotificationStatus status) {
        return new ActivityNotification(
                id,
                activitySlotId,
                notifyAt,
                sentAt,
                status
        );
    }

    public void assignId(Long id) {
        this.id = id;
    }
}
