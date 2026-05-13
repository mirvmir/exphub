package io.github.mirvmir.activity.application.persistence.mapper;

import io.github.mirvmir.activity.domain.ActivityTime;
import io.github.mirvmir.activity.application.persistence.entity.ActivityTimeEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActivityTimeMapper {

    ActivityTimeEntity toEntity(ActivityTime activityTime);

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