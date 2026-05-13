package io.github.mirvmir.taxonomy.application.persistence.mapper;

import io.github.mirvmir.taxonomy.domain.Section;
import io.github.mirvmir.taxonomy.domain.Topic;
import io.github.mirvmir.taxonomy.application.persistence.entity.SectionEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SectionMapper {

    SectionEntity toEntity(Section section);

    default Section toDomain(SectionEntity entity, List<Topic> topics) {
        if (entity == null) {
            return null;
        }

        return Section.load(
                entity.getId(),
                entity.getSubjectId(),
                entity.getName(),
                topics
        );
    }

    default Section toDomainWithoutTopics(SectionEntity entity) {
        return toDomain(entity, List.of());
    }
}