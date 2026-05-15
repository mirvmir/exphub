package io.github.mirvmir.course.domain.content;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.course.domain.HtmlSanitizer;
import io.github.mirvmir.course.exception.CourseErrorCode;
import lombok.Getter;

@Getter
public final class LessonHtml implements LessonContent {
    private final Long id;
    private final String html;

    private LessonHtml(Long id, String html) {
        this.id = id;
        this.html = html;
    }

    public static LessonHtml create(String html) {
        String sanitizedHtml = HtmlSanitizer.sanitize(html);

        if (sanitizedHtml.isBlank()) {
            throw new BusinessException(CourseErrorCode.INVALID_LESSON_BLOCK_HTML);
        }

        return new LessonHtml(null, sanitizedHtml);
    }

    public static LessonHtml load(Long id, String html) {
        return new LessonHtml(id, html);
    }

    @Override
    public LessonContentType type() {
        return LessonContentType.HTML;
    }

    @Override
    public LessonContent copyToDraft() {
        return LessonHtml.create(this.html);
    }
}