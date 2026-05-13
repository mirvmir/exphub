package io.github.mirvmir.taxonomy.web.request;

public record CreateTopicSuggestionRequest(
        String name,
        String description
) {
}