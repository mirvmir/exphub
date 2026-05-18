package io.github.mirvmir.course.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record SaveDraftModuleLessonsRequest(
        List<@Valid SaveDraftLessonItemRequest> lessons,
        UUID stableModuleId
) {
}
