package io.github.mirvmir.taxonomy.application.service.implementation;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.common.exception.UnauthorizedException;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.taxonomy.application.service.mapper.TaxonomyResponseMapper;
import io.github.mirvmir.taxonomy.application.service.port.repository.SectionRepository;
import io.github.mirvmir.taxonomy.application.service.port.repository.SubjectRepository;
import io.github.mirvmir.taxonomy.application.service.port.repository.TopicRepository;
import io.github.mirvmir.taxonomy.application.service.port.repository.TopicSuggestionRepository;
import io.github.mirvmir.taxonomy.application.service.interfaces.TaxonomyService;
import io.github.mirvmir.taxonomy.domain.Section;
import io.github.mirvmir.taxonomy.domain.Subject;
import io.github.mirvmir.taxonomy.domain.Topic;
import io.github.mirvmir.taxonomy.domain.TopicSuggestion;
import io.github.mirvmir.taxonomy.exception.TaxonomyErrorCode;
import io.github.mirvmir.taxonomy.web.request.CreateTopicSuggestionRequest;
import io.github.mirvmir.taxonomy.web.response.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
public class DefaultTaxonomyService implements TaxonomyService {

    private final IdentityApi identityApi;

    private final SubjectRepository subjectRepository;
    private final SectionRepository sectionRepository;
    private final TopicRepository topicRepository;
    private final TopicSuggestionRepository topicSuggestionRepository;

    private final TaxonomyResponseMapper taxonomyResponseMapper;

    @Override
    @Transactional
    public TopicSuggestionResponse suggestTopic(Long subjectId,
                                                Long sectionId,
                                                CreateTopicSuggestionRequest request) {
        Section section = sectionRepository.findByIdAndSubjectId(
                sectionId,
                subjectId
        );

        if (section == null) {
            throw new BusinessException(TaxonomyErrorCode.SECTION_NOT_FOUND);
        }

        Long currentUserId = identityApi.getCurrentUserId();

        if (currentUserId == null) {
            throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
        }

        TopicSuggestion suggestion = TopicSuggestion.create(
                currentUserId,
                subjectId,
                sectionId,
                request.name(),
                request.description()
        );

        TopicSuggestion savedSuggestion =
                topicSuggestionRepository.saveOrUpdate(suggestion);

        return taxonomyResponseMapper.toTopicSuggestionResponse(savedSuggestion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TopicSuggestionResponse> getMyTopicSuggestions() {
        Long currentUserId = identityApi.getCurrentUserId();

        if (currentUserId == null) {
            throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
        }

        return topicSuggestionRepository.findAllByCreatedByUserId(currentUserId)
                .stream()
                .map(taxonomyResponseMapper::toTopicSuggestionResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> getSubjects() {
        return subjectRepository.findAll()
                .stream()
                .map(taxonomyResponseMapper::toSubjectResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectDetailsResponse getSubject(Long subjectId) {
        Subject subject = subjectRepository.findByIdWithSectionsAndTopics(subjectId);

        if (subject == null) {
            throw new NotFoundException(TaxonomyErrorCode.SUBJECT_NOT_FOUND);
        }

        return taxonomyResponseMapper.toSubjectDetailsResponse(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public SectionDetailsResponse getSection(Long subjectId,
                                             Long sectionId) {
        Section section = sectionRepository.findByIdAndSubjectId(
                sectionId,
                subjectId
        );

        if (section == null) {
            throw new BusinessException(TaxonomyErrorCode.SECTION_NOT_FOUND);
        }

        List<Topic> topics = topicRepository.findAllBySectionId(sectionId);

        Section sectionWithTopics = Section.load(
                section.getId(),
                section.getSubjectId(),
                section.getName(),
                topics
        );

        return taxonomyResponseMapper.toSectionDetailsResponse(sectionWithTopics);
    }

    @Override
    @Transactional(readOnly = true)
    public TopicDetailsResponse getTopic(Long subjectId,
                                         Long sectionId,
                                         Long topicId) {
        Topic topic = topicRepository.findByIdAndSectionIdAndSubjectId(
                topicId,
                sectionId,
                subjectId
        );

        if (topic == null) {
            throw new NotFoundException(TaxonomyErrorCode.TOPIC_NOT_FOUND);
        }

        return taxonomyResponseMapper.toTopicDetailsResponse(topic);
    }
}
