package io.github.mirvmir.taxonomy.application.service.interfaces;

import io.github.mirvmir.taxonomy.web.request.*;
import io.github.mirvmir.taxonomy.web.response.SectionDetailsResponse;
import io.github.mirvmir.taxonomy.web.response.SubjectResponse;
import io.github.mirvmir.taxonomy.web.response.TopicDetailsResponse;
import io.github.mirvmir.taxonomy.web.response.TopicSuggestionResponse;

import java.util.List;

public interface TaxonomyModerationService {
    List<TopicSuggestionResponse> getPendingSuggestions();
    TopicDetailsResponse approveSuggestion(Long suggestionId);
    TopicSuggestionResponse mergeSuggestion(Long suggestionId,
                                            MergeTopicSuggestionRequest request);
    TopicSuggestionResponse rejectSuggestion(Long suggestionId,
                                             RejectTopicSuggestionRequest request);
    SubjectResponse createSubject(CreateSubjectRequest request);
    SectionDetailsResponse createSection(Long subjectId,
                                         CreateSectionRequest request);
    TopicDetailsResponse createTopic(Long subjectId,
                                     Long sectionId,
                                     CreateTopicRequest request);
}
