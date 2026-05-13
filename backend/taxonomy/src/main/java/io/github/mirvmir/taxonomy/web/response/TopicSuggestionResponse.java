package io.github.mirvmir.taxonomy.web.response;

import io.github.mirvmir.taxonomy.domain.SuggestionsStatus;

public record TopicSuggestionResponse(
        Long id,
        Long subjectId,
        Long sectionId,
        String name,
        String description,
        SuggestionsStatus status
) {
}