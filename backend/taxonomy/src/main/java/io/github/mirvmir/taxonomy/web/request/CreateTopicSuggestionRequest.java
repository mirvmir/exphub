package io.github.mirvmir.taxonomy.web.request;

import jakarta.validation.constraints.NotBlank;

public record CreateTopicSuggestionRequest(
        @NotBlank
        String name,
        String description
) {
}