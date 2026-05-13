package io.github.mirvmir.course.domain.content;

public record VideoContentData(Long videoAssetId) implements LessonContentData {
    @Override
    public LessonContentType type() {
        return LessonContentType.VIDEO;
    }
}