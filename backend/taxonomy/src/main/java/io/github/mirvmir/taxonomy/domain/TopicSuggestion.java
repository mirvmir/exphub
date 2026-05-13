package io.github.mirvmir.taxonomy.domain;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.taxonomy.exception.TaxonomyErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class TopicSuggestion {
    private Long id;
    @NonNull
    private Long createdByUserId;
    @NonNull
    private Long subjectId;
    @NonNull
    private Long sectionId;
    @NonNull
    private String name;
    private String description;
    @NonNull
    private SuggestionsStatus status;
    private String moderationComment;
    private Long resolvedTopicId;

    public static TopicSuggestion create(Long createdByUserId,
                                         Long subjectId,
                                         Long sectionId,
                                         String name,
                                         String description) {
        return new TopicSuggestion(
                null,
                createdByUserId,
                subjectId,
                sectionId,
                name,
                description,
                SuggestionsStatus.PENDING,
                null,
                null
        );
    }

    public static TopicSuggestion load(Long id,
                                       Long createdByUserId,
                                       Long subjectId,
                                       Long sectionId,
                                       String name,
                                       String description,
                                       SuggestionsStatus status,
                                       String moderationComment,
                                       Long resolvedTopicId) {
        return new TopicSuggestion(
                id,
                createdByUserId,
                subjectId,
                sectionId,
                name,
                description,
                status,
                moderationComment,
                resolvedTopicId
        );
    }

    public Topic approveModeration() {
        validatePendingStatus();

        this.status = SuggestionsStatus.APPROVED;

        return Topic.create(
                subjectId,
                sectionId,
                description,
                name
        );
    }

    public void rejectModeration(String moderationComment) {
        validatePendingStatus();

        this.status = SuggestionsStatus.REJECTED;
        this.moderationComment = moderationComment;
    }

    public void mergedModeration(Long resolvedTopicId) {
        validatePendingStatus();

        if (resolvedTopicId == null) {
            throw new IllegalArgumentException("resolvedTopicId cannot be null");
        }

        this.status = SuggestionsStatus.MERGED;
        this.resolvedTopicId = resolvedTopicId;
    }

    private void validatePendingStatus() {
        if (status != SuggestionsStatus.PENDING) {
            throw new BusinessException(TaxonomyErrorCode.TOPIC_SUGGESTION_ALREADY_MODERATED);
        }
    }
}