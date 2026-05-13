package io.github.mirvmir.taxonomy.application.persistence.mapper;

import io.github.mirvmir.taxonomy.domain.Topic;
import io.github.mirvmir.taxonomy.application.persistence.entity.TopicEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TopicMapper {

    TopicEntity toEntity(Topic topic);

    default Topic toDomain(TopicEntity entity) {
        if (entity == null) {
            return null;
        }

        return Topic.load(
                entity.getId(),
                entity.getSubjectId(),
                entity.getSectionId(),
                entity.getDescription(),
                entity.getName()
        );
    }

    List<Topic> toDomainList(List<TopicEntity> entities);
}