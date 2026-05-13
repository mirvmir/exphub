package io.github.mirvmir.taxonomy.application.service.implementation;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.taxonomy.application.service.mapper.TaxonomyResponseMapper;
import io.github.mirvmir.taxonomy.application.service.port.repository.SectionRepository;
import io.github.mirvmir.taxonomy.application.service.port.repository.SubjectRepository;
import io.github.mirvmir.taxonomy.application.service.port.repository.TopicRepository;
import io.github.mirvmir.taxonomy.application.service.port.repository.TopicSuggestionRepository;
import io.github.mirvmir.taxonomy.domain.*;
import io.github.mirvmir.taxonomy.web.request.*;
import io.github.mirvmir.taxonomy.web.response.SectionDetailsResponse;
import io.github.mirvmir.taxonomy.web.response.SubjectResponse;
import io.github.mirvmir.taxonomy.web.response.TopicDetailsResponse;
import io.github.mirvmir.taxonomy.web.response.TopicSuggestionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefaultTaxonomyModerationServiceTest {

    private SubjectRepository subjectRepository;
    private SectionRepository sectionRepository;
    private TopicRepository topicRepository;
    private TopicSuggestionRepository topicSuggestionRepository;
    private TaxonomyResponseMapper mapper;
    private DefaultTaxonomyModerationService service;

    @BeforeEach
    void setUp() {
        subjectRepository = mock(SubjectRepository.class);
        sectionRepository = mock(SectionRepository.class);
        topicRepository = mock(TopicRepository.class);
        topicSuggestionRepository = mock(TopicSuggestionRepository.class);
        mapper = new TaxonomyResponseMapper() {};

        service = new DefaultTaxonomyModerationService(
                subjectRepository,
                sectionRepository,
                topicRepository,
                topicSuggestionRepository,
                mapper
        );
    }

    @Test
    void getPendingSuggestions_shouldReturnPendingSuggestions() {
        TopicSuggestion suggestion = pendingSuggestion();

        when(topicSuggestionRepository.findAllByStatus(SuggestionsStatus.PENDING))
                .thenReturn(List.of(suggestion));

        List<TopicSuggestionResponse> result = service.getPendingSuggestions();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals(SuggestionsStatus.PENDING, result.get(0).status());
    }

    @Test
    void approveSuggestion_shouldCreateTopicAndApproveSuggestion() {
        TopicSuggestion suggestion = pendingSuggestion();

        when(topicSuggestionRepository.findById(1L))
                .thenReturn(suggestion);
        when(topicRepository.saveOrUpdate(any()))
                .thenAnswer(invocation -> {
                    Topic topic = invocation.getArgument(0);
                    topic.assignId(100L);
                    return topic;
                });
        when(topicSuggestionRepository.saveOrUpdate(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TopicDetailsResponse response = service.approveSuggestion(1L);

        assertEquals(100L, response.id());
        assertEquals(10L, response.subjectId());
        assertEquals(20L, response.sectionId());
        assertEquals("Hibernate", response.name());
        assertEquals(SuggestionsStatus.APPROVED, suggestion.getStatus());

        verify(topicRepository).saveOrUpdate(any());
        verify(topicSuggestionRepository).saveOrUpdate(suggestion);
    }

    @Test
    void approveSuggestion_shouldThrowBusinessException_whenSuggestionNotFound() {
        when(topicSuggestionRepository.findById(1L))
                .thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.approveSuggestion(1L)
        );

        assertEquals("TOPIC_SUGGESTION_NOT_FOUND", exception.getErrorCode().code());
        verifyNoInteractions(topicRepository);
    }

    @Test
    void approveSuggestion_shouldThrowBusinessException_whenAlreadyModerated() {
        TopicSuggestion suggestion = TopicSuggestion.load(
                1L,
                100L,
                10L,
                20L,
                "Hibernate",
                "ORM",
                SuggestionsStatus.APPROVED,
                null,
                null
        );

        when(topicSuggestionRepository.findById(1L))
                .thenReturn(suggestion);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.approveSuggestion(1L)
        );

        assertEquals("TOPIC_SUGGESTION_ALREADY_MODERATED", exception.getErrorCode().code());
    }

    @Test
    void mergeSuggestion_shouldMergeSuggestion() {
        TopicSuggestion suggestion = pendingSuggestion();
        Topic resolvedTopic = Topic.load(100L, 10L, 20L, "Spring Framework", "Spring");

        when(topicSuggestionRepository.findById(1L))
                .thenReturn(suggestion);
        when(topicRepository.findByIdAndSectionIdAndSubjectId(100L, 20L, 10L))
                .thenReturn(resolvedTopic);
        when(topicSuggestionRepository.saveOrUpdate(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TopicSuggestionResponse response = service.mergeSuggestion(
                1L,
                new MergeTopicSuggestionRequest(100L)
        );

        assertEquals(SuggestionsStatus.MERGED, response.status());
        assertEquals(100L, suggestion.getResolvedTopicId());

        verify(topicSuggestionRepository).saveOrUpdate(suggestion);
    }

    @Test
    void mergeSuggestion_shouldThrowBusinessException_whenTopicNotFound() {
        TopicSuggestion suggestion = pendingSuggestion();

        when(topicSuggestionRepository.findById(1L))
                .thenReturn(suggestion);
        when(topicRepository.findByIdAndSectionIdAndSubjectId(100L, 20L, 10L))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.mergeSuggestion(
                        1L,
                        new MergeTopicSuggestionRequest(100L)
                )
        );

        assertEquals("TOPIC_NOT_FOUND", exception.getErrorCode().code());
    }

    @Test
    void rejectSuggestion_shouldRejectSuggestion() {
        TopicSuggestion suggestion = pendingSuggestion();

        when(topicSuggestionRepository.findById(1L))
                .thenReturn(suggestion);
        when(topicSuggestionRepository.saveOrUpdate(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TopicSuggestionResponse response = service.rejectSuggestion(
                1L,
                new RejectTopicSuggestionRequest("Такая тема уже есть")
        );

        assertEquals(SuggestionsStatus.REJECTED, response.status());
        assertEquals("Такая тема уже есть", suggestion.getModerationComment());

        verify(topicSuggestionRepository).saveOrUpdate(suggestion);
    }

    @Test
    void createSubject_shouldCreateSubject() {
        when(subjectRepository.saveOrUpdate(any()))
                .thenAnswer(invocation -> {
                    Subject subject = invocation.getArgument(0);
                    subject.assignId(1L);
                    return subject;
                });

        SubjectResponse response = service.createSubject(
                new CreateSubjectRequest("Java")
        );

        assertEquals(1L, response.id());
        assertEquals("Java", response.name());

        verify(subjectRepository).saveOrUpdate(any());
    }

    @Test
    void createSection_shouldCreateSection() {
        when(subjectRepository.findById(1L))
                .thenReturn(Subject.load(1L, "Java", List.of()));
        when(sectionRepository.saveOrUpdate(any()))
                .thenAnswer(invocation -> {
                    Section section = invocation.getArgument(0);
                    section.assignId(2L);
                    return section;
                });

        SectionDetailsResponse response = service.createSection(
                1L,
                new CreateSectionRequest("Backend")
        );

        assertEquals(2L, response.id());
        assertEquals(1L, response.subjectId());
        assertEquals("Backend", response.name());
    }

    @Test
    void createSection_shouldThrowBusinessException_whenSubjectNotFound() {
        when(subjectRepository.findById(1L))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createSection(
                        1L,
                        new CreateSectionRequest("Backend")
                )
        );

        assertEquals("SUBJECT_NOT_FOUND", exception.getErrorCode().code());
        verifyNoInteractions(sectionRepository);
    }

    @Test
    void createTopic_shouldCreateTopic() {
        when(sectionRepository.findByIdAndSubjectId(2L, 1L))
                .thenReturn(Section.load(2L, 1L, "Backend", List.of()));
        when(topicRepository.saveOrUpdate(any()))
                .thenAnswer(invocation -> {
                    Topic topic = invocation.getArgument(0);
                    topic.assignId(3L);
                    return topic;
                });

        TopicDetailsResponse response = service.createTopic(
                1L,
                2L,
                new CreateTopicRequest("Spring", "Spring Framework")
        );

        assertEquals(3L, response.id());
        assertEquals(1L, response.subjectId());
        assertEquals(2L, response.sectionId());
        assertEquals("Spring", response.name());
        assertEquals("Spring Framework", response.description());
    }

    @Test
    void createTopic_shouldThrowBusinessException_whenSectionNotFound() {
        when(sectionRepository.findByIdAndSubjectId(2L, 1L))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createTopic(
                        1L,
                        2L,
                        new CreateTopicRequest("Spring", "Spring Framework")
                )
        );

        assertEquals("SECTION_NOT_FOUND", exception.getErrorCode().code());
        verifyNoInteractions(topicRepository);
    }

    private TopicSuggestion pendingSuggestion() {
        return TopicSuggestion.load(
                1L,
                100L,
                10L,
                20L,
                "Hibernate",
                "ORM",
                SuggestionsStatus.PENDING,
                null,
                null
        );
    }
}