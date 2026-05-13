package io.github.mirvmir.course.web.request;

public record SaveDraftModuleItemRequest(
        Long id,
        String uiId,
        String title,
        Integer sortOrder
) {
}