package io.github.mirvmir.notification.persistence.mapper;

import io.github.mirvmir.notification.domain.ActivityNotification;
import io.github.mirvmir.notification.persistence.entity.ActivityNotificationEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActivityNotificationMapper {

    ActivityNotificationEntity toEntity(ActivityNotification activityNotification);

    default ActivityNotification toDomain(ActivityNotificationEntity entity) {
        if (entity == null) {
            return null;
        }

        return ActivityNotification.load(
                entity.getId(),
                entity.getActivitySlotId(),
                entity.getNotifyAt(),
                entity.getSentAt(),
                entity.getStatus()
        );
    }
}