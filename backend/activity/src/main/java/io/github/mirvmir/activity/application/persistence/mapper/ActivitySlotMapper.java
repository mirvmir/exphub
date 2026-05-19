package io.github.mirvmir.activity.application.persistence.mapper;

import io.github.mirvmir.activity.application.persistence.entity.ActivityEntity;
import io.github.mirvmir.activity.application.persistence.entity.ActivitySlotEntity;
import io.github.mirvmir.activity.domain.ActivitySlot;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActivitySlotMapper {

    default ActivitySlotEntity toEntity(ActivitySlot slot) {
        if (slot == null) {
            return null;
        }

        return new ActivitySlotEntity(
                slot.getId(),
                activityFromId(slot.getActivityId()),
                slot.getTeacherId(),
                slot.getCreatedByUserId(),
                slot.getStartAt(),
                slot.getEndAt(),
                slot.getCreatedAt(),
                slot.getStatus(),
                slot.getRoomJoinUrl()
        );
    }

    default ActivitySlot toDomain(ActivitySlotEntity entity) {
        if (entity == null) {
            return null;
        }

        return ActivitySlot.load(
                entity.getId(),
                entity.getActivityEntity().getId(),
                entity.getTeacherId(),
                entity.getCreatedByUserId(),
                entity.getStartAt(),
                entity.getEndAt(),
                entity.getCreatedAt(),
                entity.getRoomJoinUrl(),
                entity.getStatus()
        );
    }

    default ActivityEntity activityFromId(Long activityId) {
        if (activityId == null) {
            return null;
        }

        return new ActivityEntity(
                activityId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}