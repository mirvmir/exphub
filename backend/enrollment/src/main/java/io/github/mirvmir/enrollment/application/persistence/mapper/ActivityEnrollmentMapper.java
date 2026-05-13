package io.github.mirvmir.enrollment.application.persistence.mapper;

import io.github.mirvmir.enrollment.domain.ActivityEnrollment;
import io.github.mirvmir.enrollment.application.persistence.entity.ActivityEnrollmentEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActivityEnrollmentMapper {

    ActivityEnrollmentEntity toEntity(ActivityEnrollment activityEnrollment);

    default ActivityEnrollment toDomain(ActivityEnrollmentEntity entity) {
        if (entity == null) {
            return null;
        }

        return ActivityEnrollment.load(
                entity.getId(),
                entity.getActivitySlotId(),
                entity.getUserId(),
                entity.getStatus(),
                entity.getSubscribedAt()
        );
    }
}