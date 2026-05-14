package io.github.mirvmir.taxonomy.web.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MergeTopicSuggestionRequest(
        @NotNull
        @Positive
        Long resolvedTopicId
) {
}
