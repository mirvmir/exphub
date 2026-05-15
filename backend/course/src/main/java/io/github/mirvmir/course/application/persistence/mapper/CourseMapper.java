package io.github.mirvmir.course.application.persistence.mapper;

import io.github.mirvmir.course.application.persistence.entity.CourseVersionEntity;
import io.github.mirvmir.course.domain.Course;
import io.github.mirvmir.course.domain.CourseLessonOpening;
import io.github.mirvmir.course.application.persistence.entity.CourseEntity;
import io.github.mirvmir.course.application.persistence.entity.CourseLessonOpeningEntity;
import io.github.mirvmir.course.application.persistence.entity.CourseTopicEntity;
import io.github.mirvmir.course.domain.CourseVersion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
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
                entity.getSubjectId(),
                toTopicIds(entity.getTopicEntities()),
                toLessonOpenings(entity.getLessonOpeningEntities()),
                courseVersionMapper.toDomain(entity.getPublishedVersion()),
                courseVersionMapper.toDomain(entity.getDraftVersion())
        );
    }

    public Course toDomainWithDraftInfo(CourseEntity entity) {
        if (entity == null) {
            return null;
        }

        return Course.load(
                entity.getId(),
                entity.getAuthorId(),
                entity.getStatus(),
                entity.getSubjectId(),
                toTopicIds(entity.getTopicEntities()),
                toLessonOpenings(entity.getLessonOpeningEntities()),
                null,
                toVersionInfo(entity.getDraftVersion())
        );
    }

    public Course toDomainWithPublishedInfo(CourseEntity entity) {
        if (entity == null) {
            return null;
        }

        return Course.load(
                entity.getId(),
                entity.getAuthorId(),
                entity.getStatus(),
                entity.getSubjectId(),
                toTopicIds(entity.getTopicEntities()),
                toLessonOpenings(entity.getLessonOpeningEntities()),
                toVersionInfo(entity.getPublishedVersion()),
                null
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

    private CourseVersion toVersionInfo(CourseVersionEntity entity) {
        if (entity == null) {
            return null;
        }

        return CourseVersion.load(
                entity.getId(),
                entity.getModerationStatus(),
                entity.getTitle(),
                entity.getShortDescription(),
                entity.getDescriptionHtml(),
                entity.getPrice() == null ? null : entity.getPrice().getAmount(),
                entity.getPrice() == null ? null : entity.getPrice().getCurrency(),
                entity.getModerationComment(),
                new ArrayList<>()
        );
    }
}