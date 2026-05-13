package io.github.mirvmir.taxonomy.application.service.implementation;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.taxonomy.application.service.mapper.TaxonomyResponseMapper;
import io.github.mirvmir.taxonomy.application.service.port.repository.SectionRepository;
import io.github.mirvmir.taxonomy.application.service.port.repository.SubjectRepository;
import io.github.mirvmir.taxonomy.application.service.port.repository.TopicRepository;
import io.github.mirvmir.taxonomy.application.service.port.repository.TopicSuggestionRepository;
import io.github.mirvmir.taxonomy.application.service.interfaces.TaxonomyModerationService;
import io.github.mirvmir.taxonomy.domain.*;
import io.github.mirvmir.taxonomy.exception.TaxonomyErrorCode;
import io.github.mirvmir.taxonomy.web.request.*;
import io.github.mirvmir.taxonomy.web.response.SectionDetailsResponse;
import io.github.mirvmir.taxonomy.web.response.SubjectResponse;
import io.github.mirvmir.taxonomy.web.response.TopicDetailsResponse;
import io.github.mirvmir.taxonomy.web.response.TopicSuggestionResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
public class DefaultTaxonomyModerationService implements TaxonomyModerationService {

    private final SubjectRepository subjectRepository;
    private final SectionRepository sectionRepository;
    private final TopicRepository topicRepository;
    private final TopicSuggestionRepository topicSuggestionRepository;

    private final TaxonomyResponseMapper taxonomyResponseMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TopicSuggestionResponse> getPendingSuggestions() {
        return topicSuggestionRepository.findAllByStatus(SuggestionsStatus.PENDING)
                .stream()
                .map(taxonomyResponseMapper::toTopicSuggestionResponse)
                .toList();
    }

    @Override
    @Transactional
    public TopicDetailsResponse approveSuggestion(Long suggestionId) {
        TopicSuggestion suggestion = getExistingSuggestion(suggestionId);

        Topic topic = suggestion.approveModeration();

        Topic savedTopic = topicRepository.saveOrUpdate(topic);
        topicSuggestionRepository.saveOrUpdate(suggestion);

        return taxonomyResponseMapper.toTopicDetailsResponse(savedTopic);
    }

    @Override
    @Transactional
    public TopicSuggestionResponse mergeSuggestion(
            Long suggestionId,
            MergeTopicSuggestionRequest request
    ) {
        TopicSuggestion suggestion = getExistingSuggestion(suggestionId);

        Topic topic = topicRepository.findByIdAndSectionIdAndSubjectId(
                request.resolvedTopicId(),
                suggestion.getSectionId(),
                suggestion.getSubjectId()
        );

        if (topic == null) {
            throw new BusinessException(TaxonomyErrorCode.TOPIC_NOT_FOUND);
        }

        suggestion.mergedModeration(request.resolvedTopicId());

        TopicSuggestion savedSuggestion =
                topicSuggestionRepository.saveOrUpdate(suggestion);

        return taxonomyResponseMapper.toTopicSuggestionResponse(savedSuggestion);
    }

    @Override
    @Transactional
    public TopicSuggestionResponse rejectSuggestion(
            Long suggestionId,
            RejectTopicSuggestionRequest request
    ) {
        TopicSuggestion suggestion = getExistingSuggestion(suggestionId);

        suggestion.rejectModeration(request.moderationComment());

        TopicSuggestion savedSuggestion =
                topicSuggestionRepository.saveOrUpdate(suggestion);

        return taxonomyResponseMapper.toTopicSuggestionResponse(savedSuggestion);
    }

    private TopicSuggestion getExistingSuggestion(Long suggestionId) {
        TopicSuggestion suggestion = topicSuggestionRepository.findById(suggestionId);

        if (suggestion == null) {
            throw new NotFoundException(TaxonomyErrorCode.TOPIC_SUGGESTION_NOT_FOUND);
        }

        return suggestion;
    }

    @Override
    @Transactional
    public SubjectResponse createSubject(CreateSubjectRequest request) {
        Subject subject = Subject.create(request.name());

        Subject savedSubject = subjectRepository.saveOrUpdate(subject);

        return taxonomyResponseMapper.toSubjectResponse(savedSubject);
    }

    @Override
    @Transactional
    public SectionDetailsResponse createSection(Long subjectId,
                                                CreateSectionRequest request) {
        Subject subject = subjectRepository.findById(subjectId);

        if (subject == null) {
            throw new BusinessException(TaxonomyErrorCode.SUBJECT_NOT_FOUND);
        }

        Section section = Section.create(
                subjectId,
                request.name()
        );

        Section savedSection = sectionRepository.saveOrUpdate(section);

        return taxonomyResponseMapper.toSectionDetailsResponse(savedSection);
    }

    @Override
    @Transactional
    public TopicDetailsResponse createTopic(Long subjectId,
                                            Long sectionId,
                                            CreateTopicRequest request) {
        Section section = sectionRepository.findByIdAndSubjectId(
                sectionId,
                subjectId
        );

        if (section == null) {
            throw new BusinessException(TaxonomyErrorCode.SECTION_NOT_FOUND);
        }

        Topic topic = Topic.create(
                subjectId,
                sectionId,
                request.description(),
                request.name()
        );

        Topic savedTopic = topicRepository.saveOrUpdate(topic);

        return taxonomyResponseMapper.toTopicDetailsResponse(savedTopic);
    }
}
