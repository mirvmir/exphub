package io.github.mirvmir.course.application.persistence.mapper;

import io.github.mirvmir.course.domain.CourseVersion;
import io.github.mirvmir.course.application.persistence.entity.CourseModuleEntity;
import io.github.mirvmir.course.application.persistence.entity.CourseVersionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CourseVersionMapper {

    private final CourseModuleMapper courseModuleMapper;

    public CourseVersionEntity toEntity(CourseVersion version) {
        if (version == null) {
            return null;
        }

        List<CourseModuleEntity> moduleEntities = version.getModules()
                .stream()
                .map(courseModuleMapper::toEntity)
                .toList();

        CourseVersionEntity entity = new CourseVersionEntity(
                version.getId(),
                version.getTitle(),
                version.getPrice(),
                version.getStatus(),
                version.getModerationComment(),
                version.getShortDescription(),
                version.getDescriptionHtml(),
                moduleEntities
        );

        for (CourseModuleEntity moduleEntity : moduleEntities) {
            moduleEntity.assignCourseVersion(entity);
        }

        return entity;
    }

    public CourseVersion toDomain(CourseVersionEntity entity) {
        if (entity == null) {
            return null;
        }

        return CourseVersion.load(
                entity.getId(),
                entity.getModerationStatus(),
                entity.getTitle(),
                entity.getShortDescription(),
                entity.getDescriptionHtml(),
                entity.getPrice().getAmount(),
                entity.getPrice().getCurrency(),
                entity.getModerationComment(),
                entity.getModules()
                        .stream()
                        .map(courseModuleMapper::toDomain)
                        .toList()
        );
    }
}