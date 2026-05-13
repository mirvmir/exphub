package io.github.mirvmir.course.domain.content;

public record HtmlContentData(String html) implements LessonContentData {
    @Override
    public LessonContentType type() {
        return LessonContentType.HTML;
    }
}
