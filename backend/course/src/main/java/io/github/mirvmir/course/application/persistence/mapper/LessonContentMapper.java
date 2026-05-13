package io.github.mirvmir.course.application.persistence.mapper;

import io.github.mirvmir.course.domain.content.LessonContent;
import io.github.mirvmir.course.domain.content.LessonFile;
import io.github.mirvmir.course.domain.content.LessonHtml;
import io.github.mirvmir.course.domain.content.LessonVideo;
import io.github.mirvmir.course.application.persistence.entity.FileLessonEntity;
import io.github.mirvmir.course.application.persistence.entity.HtmlLessonEntity;
import io.github.mirvmir.course.application.persistence.entity.VideoLessonEntity;
import org.springframework.stereotype.Component;

@Component
public class LessonContentMapper {

    public LessonContent toDomain(
            HtmlLessonEntity htmlLesson,
            FileLessonEntity fileLesson,
            VideoLessonEntity videoLesson
    ) {
        if (htmlLesson != null) {
            return LessonHtml.load(htmlLesson.getContent());
        }

        if (fileLesson != null) {
            return LessonFile.load(fileLesson.getFileAssetId());
        }

        if (videoLesson != null) {
            return LessonVideo.load(videoLesson.getVideoAssetId());
        }

        return null;
    }

    public HtmlLessonEntity toHtmlEntity(LessonContent content) {
        if (content instanceof LessonHtml lessonHtml) {
            return new HtmlLessonEntity(
                    null,
                    null,
                    lessonHtml.getHtml()
            );
        }

        return null;
    }

    public FileLessonEntity toFileEntity(LessonContent content) {
        if (content instanceof LessonFile lessonFile) {
            return new FileLessonEntity(
                    null,
                    null,
                    lessonFile.getFileAssetId()
            );
        }

        return null;
    }

    public VideoLessonEntity toVideoEntity(LessonContent content) {
        if (content instanceof LessonVideo lessonVideo) {
            return new VideoLessonEntity(
                    null,
                    null,
                    lessonVideo.getVideoAssetId()
            );
        }

        return null;
    }
}