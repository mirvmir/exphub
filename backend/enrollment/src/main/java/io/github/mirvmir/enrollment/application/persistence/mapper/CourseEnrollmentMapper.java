package io.github.mirvmir.enrollment.application.persistence.mapper;

import io.github.mirvmir.enrollment.domain.CourseEnrollment;
import io.github.mirvmir.enrollment.application.persistence.entity.CourseEnrollmentEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseEnrollmentMapper {

    CourseEnrollmentEntity toEntity(CourseEnrollment courseEnrollment);

    default CourseEnrollment toDomain(CourseEnrollmentEntity entity) {
        if (entity == null) {
            return null;
        }

        return CourseEnrollment.load(
                entity.getId(),
                entity.getCourseId(),
                entity.getPublishedVersionId(),
                entity.getUserId(),
                entity.getStatus(),
                entity.getProgressPercent(),
                entity.getSubscribedAt()
        );
    }
}