package io.github.mirvmir.course.application.persistence.mapper;

import io.github.mirvmir.course.domain.LessonBlock;
import io.github.mirvmir.course.domain.content.LessonContent;
import io.github.mirvmir.course.application.persistence.entity.FileLessonEntity;
import io.github.mirvmir.course.application.persistence.entity.HtmlLessonEntity;
import io.github.mirvmir.course.application.persistence.entity.LessonBlockEntity;
import io.github.mirvmir.course.application.persistence.entity.VideoLessonEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LessonBlockMapper {

    private final LessonContentMapper lessonContentMapper;

    public LessonBlockEntity toEntity(LessonBlock block) {
        if (block == null) {
            return null;
        }

        HtmlLessonEntity htmlLesson =
                lessonContentMapper.toHtmlEntity(block.getContent());

        FileLessonEntity fileLesson =
                lessonContentMapper.toFileEntity(block.getContent());

        VideoLessonEntity videoLesson =
                lessonContentMapper.toVideoEntity(block.getContent());

        LessonBlockEntity entity = new LessonBlockEntity(
                block.getId(),
                null,
                block.getStableBlockId(),
                block.getSortOrder(),
                block.getType(),
                block.getContentHash(),
                htmlLesson,
                fileLesson,
                videoLesson
        );

        entity.assignContent();

        return entity;
    }

    public LessonBlock toDomain(LessonBlockEntity entity) {
        if (entity == null) {
            return null;
        }

        LessonContent content = lessonContentMapper.toDomain(
                entity.getHtmlLesson(),
                entity.getFileLesson(),
                entity.getVideoLesson()
        );

        return LessonBlock.load(
                entity.getId(),
                entity.getStableBlockId(),
                content,
                entity.getSortOrder(),
                entity.getContentHash()
        );
    }
}