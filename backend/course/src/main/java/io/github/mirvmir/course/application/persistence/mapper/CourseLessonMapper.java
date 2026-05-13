package io.github.mirvmir.course.application.persistence.mapper;

import io.github.mirvmir.course.domain.CourseLesson;
import io.github.mirvmir.course.domain.LessonBlock;
import io.github.mirvmir.course.application.persistence.entity.CourseLessonEntity;
import io.github.mirvmir.course.application.persistence.entity.LessonBlockEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CourseLessonMapper {

    private final LessonBlockMapper lessonBlockMapper;

    public CourseLessonEntity toEntity(CourseLesson lesson) {
        if (lesson == null) {
            return null;
        }

        List<LessonBlockEntity> blockEntities = lesson.getBlocks()
                .stream()
                .map(lessonBlockMapper::toEntity)
                .toList();

        CourseLessonEntity entity = new CourseLessonEntity(
                lesson.getId(),
                null,
                lesson.getStableLessonId(),
                lesson.getSortOrder(),
                lesson.getTitle(),
                lesson.getType(),
                blockEntities
        );

        for (LessonBlockEntity blockEntity : blockEntities) {
            blockEntity.assignLesson(entity);
            blockEntity.assignContent();
        }

        return entity;
    }

    public CourseLesson toDomain(CourseLessonEntity entity) {
        if (entity == null) {
            return null;
        }

        List<LessonBlock> blocks = entity.getBlocks()
                .stream()
                .map(lessonBlockMapper::toDomain)
                .toList();

        return CourseLesson.load(
                entity.getId(),
                entity.getStableLessonId(),
                entity.getTitle(),
                entity.getType(),
                entity.getSortOrder(),
                blocks
        );
    }
}