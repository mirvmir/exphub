package io.github.mirvmir.course.web.request;

import io.github.mirvmir.course.domain.content.LessonContentType;

public record SaveDraftLessonBlockItemRequest(
        Long id,
        String uiId,
        LessonContentType type,
        String html,
        Long fileAssetId,
        Long videoAssetId,
        Integer sortOrder
) {
}