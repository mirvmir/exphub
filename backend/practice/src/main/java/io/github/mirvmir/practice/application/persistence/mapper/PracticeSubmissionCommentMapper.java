package io.github.mirvmir.practice.application.persistence.mapper;

import io.github.mirvmir.practice.domain.PracticeSubmissionComment;
import io.github.mirvmir.practice.application.persistence.entity.PracticeSubmissionCommentEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PracticeSubmissionCommentMapper {

    PracticeSubmissionCommentEntity toEntity(PracticeSubmissionComment practiceSubmissionComment);

    default PracticeSubmissionComment toDomain(PracticeSubmissionCommentEntity entity) {
        if (entity == null) {
            return null;
        }

        return PracticeSubmissionComment.load(
                entity.getId(),
                entity.getPracticeSubmissionAnswerId(),
                entity.getText(),
                entity.getFileId(),
                entity.getCreatedAt()
        );
    }
}