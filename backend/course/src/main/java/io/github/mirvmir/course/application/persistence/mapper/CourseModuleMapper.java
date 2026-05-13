package io.github.mirvmir.course.application.persistence.mapper;

import io.github.mirvmir.course.domain.CourseLesson;
import io.github.mirvmir.course.domain.CourseModule;
import io.github.mirvmir.course.application.persistence.entity.CourseLessonEntity;
import io.github.mirvmir.course.application.persistence.entity.CourseModuleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CourseModuleMapper {

    private final CourseLessonMapper courseLessonMapper;

    public CourseModuleEntity toEntity(CourseModule module) {
        if (module == null) {
            return null;
        }

        List<CourseLessonEntity> lessonEntities = module.getLessons()
                .stream()
                .map(courseLessonMapper::toEntity)
                .toList();

        CourseModuleEntity entity = new CourseModuleEntity(
                module.getId(),
                null,
                module.getStableModuleId(),
                module.getTitle(),
                module.getSortOrder(),
                lessonEntities
        );

        for (CourseLessonEntity lessonEntity : lessonEntities) {
            lessonEntity.assignModule(entity);
        }

        return entity;
    }

    public CourseModule toDomain(CourseModuleEntity entity) {
        if (entity == null) {
            return null;
        }

        List<CourseLesson> lessons = entity.getLessons()
                .stream()
                .map(courseLessonMapper::toDomain)
                .toList();

        return CourseModule.load(
                entity.getId(),
                entity.getStableModuleId(),
                entity.getTitle(),
                entity.getSortOrder(),
                lessons
        );
    }
}