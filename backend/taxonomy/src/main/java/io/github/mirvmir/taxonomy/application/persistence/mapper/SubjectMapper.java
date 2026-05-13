package io.github.mirvmir.taxonomy.application.persistence.mapper;

import io.github.mirvmir.taxonomy.domain.Section;
import io.github.mirvmir.taxonomy.domain.Subject;
import io.github.mirvmir.taxonomy.application.persistence.entity.SubjectEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubjectMapper {

    SubjectEntity toEntity(Subject subject);

    default Subject toDomain(SubjectEntity entity, List<Section> sections) {
        if (entity == null) {
            return null;
        }

        return Subject.load(
                entity.getId(),
                entity.getName(),
                sections
        );
    }

    default Subject toDomainWithoutSections(SubjectEntity entity) {
        return toDomain(entity, List.of());
    }
}