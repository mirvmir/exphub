package io.github.mirvmir.course.domain.content;

public record FileContentData(Long fileAssetId) implements LessonContentData {
    @Override
    public LessonContentType type() {
        return LessonContentType.FILE;
    }
}