package io.github.mirvmir.taxonomy.web.request;

import jakarta.validation.constraints.NotBlank;

public record CreateTopicRequest(
        @NotBlank
        String name,
        String description
) {
}
