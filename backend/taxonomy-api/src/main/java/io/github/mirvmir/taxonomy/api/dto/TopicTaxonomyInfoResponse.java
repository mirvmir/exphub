package io.github.mirvmir.taxonomy.api.dto;

public record TopicTaxonomyInfoResponse(
        Long topicId,
        Long sectionId,
        Long subjectId
) {
}