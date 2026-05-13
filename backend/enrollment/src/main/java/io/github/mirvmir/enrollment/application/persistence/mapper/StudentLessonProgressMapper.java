package io.github.mirvmir.enrollment.application.persistence.mapper;

import io.github.mirvmir.enrollment.domain.StudentLessonProgress;
import io.github.mirvmir.enrollment.application.persistence.entity.StudentLessonProgressEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StudentLessonProgressMapper {

    StudentLessonProgressEntity toEntity(StudentLessonProgress studentLessonProgress);

    default StudentLessonProgress toDomain(StudentLessonProgressEntity entity) {
        if (entity == null) {
            return null;
        }

        return StudentLessonProgress.load(
                entity.getId(),
                entity.getEnrollmentId(),
                entity.getCourseLessonId(),
                entity.getCompletedAt()
        );
    }
}