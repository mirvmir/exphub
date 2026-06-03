package io.github.mirvmir.activity.application.persistence.mapper;

import io.github.mirvmir.activity.domain.Activity;
import io.github.mirvmir.activity.domain.ActivityTime;
import io.github.mirvmir.activity.application.persistence.entity.ActivityEntity;
import io.github.mirvmir.activity.application.persistence.entity.ActivityTimeEntity;
import io.github.mirvmir.activity.application.persistence.entity.ActivityTopicEntity;
import io.github.mirvmir.common.domain.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        uses = ActivityTimeMapper.class
)
public interface ActivityMapper {

    @Mapping(target = "activityTimeEntities", ignore = true)
    @Mapping(target = "topicEntities", ignore = true)
    ActivityEntity toEntity(Activity activity);

    default Activity toDomain(ActivityEntity entity) {
        if (entity == null) {
            return null;
        }

        Money price = entity.getPrice();

        return Activity.load(
                entity.getId(),
                entity.getAuthorId(),
                entity.getTitle(),
                entity.getShortDescription(),
                entity.getDescriptionHtml(),
                entity.getMaxBookableSeats(),
                price.getAmount(),
                price.getCurrency(),
                entity.getDurationMinutes(),
                entity.getSubjectId(),
                entity.getType(),
                entity.getContentStatus(),
                entity.getModerationStatus(),
                entity.getModerationComment(),
                toTopicIds(entity.getTopicEntities()),
                toActivityTimes(entity.getActivityTimeEntities())
        );
    }

    default Set<ActivityTime> toActivityTimes(Set<ActivityTimeEntity> activityTimeEntities) {
        if (activityTimeEntities == null) {
            return new HashSet<>();
        }

        return activityTimeEntities.stream()
                .map(this::toActivityTime)
                .collect(Collectors.toSet());
    };

    default ActivityTime toActivityTime(ActivityTimeEntity entity) {
        if (entity == null) {
            return null;
        }

        return ActivityTime.load(
                entity.getId(),
                entity.getStartAt(),
                entity.getEndAt(),
                entity.getBookingStepMinutes()
        );
    }

    default Set<Long> toTopicIds(Set<ActivityTopicEntity> topicEntities) {
        if (topicEntities == null) {
            return new HashSet<>();
        }

        return topicEntities.stream()
                .map(ActivityTopicEntity::getTopicId)
                .collect(Collectors.toSet());
    }
}