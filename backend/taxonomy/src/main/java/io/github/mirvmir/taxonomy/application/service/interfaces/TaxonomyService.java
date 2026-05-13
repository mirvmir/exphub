package io.github.mirvmir.taxonomy.application.service.interfaces;

import io.github.mirvmir.taxonomy.web.request.CreateTopicSuggestionRequest;
import io.github.mirvmir.taxonomy.web.response.*;

import java.util.List;

public interface TaxonomyService {
    List<TopicSuggestionResponse> getMyTopicSuggestions();
    TopicSuggestionResponse suggestTopic(Long subjectId,
                                         Long sectionId,
                                         CreateTopicSuggestionRequest request);
    List<SubjectResponse> getSubjects();
    SubjectDetailsResponse getSubject(Long subjectId);
    SectionDetailsResponse getSection(Long subjectId, Long sectionId);
    TopicDetailsResponse getTopic(Long subjectId,
                                  Long sectionId,
                                  Long topicId);
}
