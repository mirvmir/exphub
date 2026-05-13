package io.github.mirvmir.course.application.persistence.mapper;

import io.github.mirvmir.course.domain.Course;
import io.github.mirvmir.course.domain.CourseLessonOpening;
import io.github.mirvmir.course.application.persistence.entity.CourseEntity;
import io.github.mirvmir.course.application.persistence.entity.CourseLessonOpeningEntity;
import io.github.mirvmir.course.application.persistence.entity.CourseTopicEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        uses = CourseVersionMapper.class
)
public abstract class CourseMapper {

    protected CourseVersionMapper courseVersionMapper;

    @Autowired
    public void setCourseVersionMapper(CourseVersionMapper courseVersionMapper) {
        this.courseVersionMapper = courseVersionMapper;
    }

    @Mapping(target = "topicEntities", ignore = true)
    @Mapping(target = "lessonOpeningEntities", ignore = true)
    public abstract CourseEntity toEntity(Course course);

    public Course toDomain(CourseEntity entity) {
        if (entity == null) {
            return null;
        }

        return Course.load(
                entity.getId(),
                entity.getAuthorId(),
                entity.getStatus(),
                toTopicIds(entity.getTopicEntities()),
                toLessonOpenings(entity.getLessonOpeningEntities()),
                courseVersionMapper.toDomain(entity.getPublishedVersion()),
                courseVersionMapper.toDomain(entity.getDraftVersion())
        );
    }

    protected Set<Long> toTopicIds(Set<CourseTopicEntity> topicEntities) {
        if (topicEntities == null) {
            return new HashSet<>();
        }

        return topicEntities.stream()
                .map(CourseTopicEntity::getTopicId)
                .collect(Collectors.toSet());
    }

    protected Set<CourseLessonOpening> toLessonOpenings(Set<CourseLessonOpeningEntity> entities) {
        if (entities == null) {
            return new HashSet<>();
        }

        return entities.stream()
                .map(entity -> CourseLessonOpening.create(
                        entity.getStableLessonId(),
                        entity.getOpensAt()
                ))
                .collect(Collectors.toSet());
    }
}