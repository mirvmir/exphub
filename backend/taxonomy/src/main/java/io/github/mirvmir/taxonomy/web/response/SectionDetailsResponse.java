package io.github.mirvmir.taxonomy.web.response;

import java.util.List;

public record SectionDetailsResponse(
        Long id,
        Long subjectId,
        String name,
        List<TopicShortResponse> topics
) {
}