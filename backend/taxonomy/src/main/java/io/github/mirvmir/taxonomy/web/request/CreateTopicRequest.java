package io.github.mirvmir.taxonomy.web.request;

public record CreateTopicRequest(
        String name,
        String description
) {
}
