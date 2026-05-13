package io.github.mirvmir.course.web.request;

import java.util.List;
import java.util.UUID;

public record SaveDraftLessonBlocksRequest(
        List<SaveDraftLessonBlockItemRequest> blocks,
        UUID stableLessonId
) {
}
