package io.github.mirvmir.taxonomy.web.response;

import java.util.List;

public record SectionShortResponse(
        Long id,
        String name,
        List<TopicShortResponse> topics
) {
}