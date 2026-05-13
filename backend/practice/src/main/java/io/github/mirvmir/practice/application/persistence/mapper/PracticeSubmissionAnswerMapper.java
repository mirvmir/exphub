package io.github.mirvmir.practice.application.persistence.mapper;

import io.github.mirvmir.practice.domain.PracticeSubmissionAnswer;
import io.github.mirvmir.practice.application.persistence.entity.PracticeSubmissionAnswerEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PracticeSubmissionAnswerMapper {

    PracticeSubmissionAnswerEntity toEntity(PracticeSubmissionAnswer practiceSubmissionAnswer);

    default PracticeSubmissionAnswer toDomain(PracticeSubmissionAnswerEntity entity) {
        if (entity == null) {
            return null;
        }

        return PracticeSubmissionAnswer.load(
                entity.getId(),
                entity.getPracticeSubmissionId(),
                entity.getHtml(),
                entity.getFileId(),
                entity.getCreatedAt()
        );
    }
}