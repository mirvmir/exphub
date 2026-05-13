package io.github.mirvmir.taxonomy.web.response;

public record TopicDetailsResponse(
        Long id,
        Long subjectId,
        Long sectionId,
        String name,
        String description
) {
}