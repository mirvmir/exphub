package io.github.mirvmir.course.web.request;

import java.util.List;
import java.util.UUID;

public record SaveDraftModuleLessonsRequest(
        List<SaveDraftLessonItemRequest> lessons,
        UUID stableModuleId
) {
}
