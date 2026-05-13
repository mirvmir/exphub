package io.github.mirvmir.course.web.response;

import io.github.mirvmir.course.domain.content.LessonContentType;

import java.util.UUID;

public record AuthorLessonBlockResponse(
        Long id,
        UUID stableBlockId,
        LessonContentType type,
        Integer sortOrder,
        String html,
        Long fileAssetId,
        Long videoAssetId
) {
}