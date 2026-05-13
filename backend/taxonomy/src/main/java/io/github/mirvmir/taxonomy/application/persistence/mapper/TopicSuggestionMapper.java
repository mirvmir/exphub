package io.github.mirvmir.taxonomy.application.persistence.mapper;

import io.github.mirvmir.taxonomy.domain.TopicSuggestion;
import io.github.mirvmir.taxonomy.application.persistence.entity.TopicSuggestionEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TopicSuggestionMapper {

    TopicSuggestionEntity toEntity(TopicSuggestion suggestion);

    default TopicSuggestion toDomain(TopicSuggestionEntity entity) {
        if (entity == null) {
            return null;
        }

        return TopicSuggestion.load(
                entity.getId(),
                entity.getCreatedByUserId(),
                entity.getSubjectId(),
                entity.getSectionId(),
                entity.getName(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getModerationComment(),
                entity.getResolvedTopicId()
        );
    }

    List<TopicSuggestion> toDomainList(List<TopicSuggestionEntity> entities);
}