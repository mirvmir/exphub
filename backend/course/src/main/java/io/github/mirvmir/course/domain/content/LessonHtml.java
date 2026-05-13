package io.github.mirvmir.course.domain.content;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.course.exception.CourseErrorCode;
import lombok.Getter;

@Getter
public final class LessonHtml implements LessonContent {
    private final String html;

    public static LessonHtml create(String html) {
        return new LessonHtml(html);
    }

    public static LessonHtml load(String html) {
        return new LessonHtml(html);
    }

    private LessonHtml(String html) {
        if (html == null || html.isBlank()) {
            throw new BusinessException(CourseErrorCode.LESSON_BLOCK_HTML_REQUIRED);
        }

        this.html = html;
    }

    @Override
    public LessonContentType type() {
        return LessonContentType.HTML;
    }
}