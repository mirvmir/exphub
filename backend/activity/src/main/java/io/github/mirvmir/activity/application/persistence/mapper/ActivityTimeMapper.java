package io.github.mirvmir.activity.application.persistence.mapper;

import io.github.mirvmir.activity.application.persistence.entity.ActivityEntity;
import io.github.mirvmir.activity.domain.ActivityTime;
import io.github.mirvmir.activity.application.persistence.entity.ActivityTimeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActivityTimeMapper {

    default ActivityTimeEntity toEntity(ActivityTime activityTime,
                                        ActivityEntity activityEntity) {
        if (activityTime == null) {
            return null;
        }

        return new ActivityTimeEntity(
                activityTime.getId(),
                activityTime.getStartAt(),
                activityTime.getEndAt(),
                activityEntity
        );
    }

    default ActivityTime toDomain(ActivityTimeEntity entity) {
        if (entity == null) {
            return null;
        }

        return ActivityTime.load(
                entity.getId(),
                entity.getStartAt(),
                entity.getEndAt()
        );
    }
}