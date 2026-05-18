package io.github.mirvmir.practice.application.persistence.mapper;

import io.github.mirvmir.practice.domain.PracticeSubmission;
import io.github.mirvmir.practice.application.persistence.entity.PracticeSubmissionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PracticeSubmissionMapper {

    PracticeSubmissionEntity toEntity(PracticeSubmission practiceSubmission);

    default PracticeSubmission toDomain(PracticeSubmissionEntity entity) {
        if (entity == null) {
            return null;
        }

        return PracticeSubmission.load(
                entity.getId(),
                entity.getStableLessonId(),
                entity.getCourseEnrollmentId(),
                entity.getStudentId(),
                entity.getCreatedAt(),
                entity.getCheckedAt()
        );
    }
}