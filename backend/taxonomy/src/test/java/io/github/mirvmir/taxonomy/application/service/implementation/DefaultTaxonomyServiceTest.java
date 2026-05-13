package io.github.mirvmir.taxonomy.application.service.implementation;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.taxonomy.application.service.mapper.TaxonomyResponseMapper;
import io.github.mirvmir.taxonomy.application.service.port.repository.SectionRepository;
import io.github.mirvmir.taxonomy.application.service.port.repository.SubjectRepository;
import io.github.mirvmir.taxonomy.application.service.port.repository.TopicRepository;
import io.github.mirvmir.taxonomy.application.service.port.repository.TopicSuggestionRepository;
import io.github.mirvmir.taxonomy.domain.*;
import io.github.mirvmir.taxonomy.web.request.CreateTopicSuggestionRequest;
import io.github.mirvmir.taxonomy.web.response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DefaultTaxonomyServiceTest {

    private IdentityApi identityApi;
    private SubjectRepository subjectRepository;
    private SectionRepository sectionRepository;
    private TopicRepository topicRepository;
    private TopicSuggestionRepository topicSuggestionRepository;
    private TaxonomyResponseMapper mapper;
    private DefaultTaxonomyService service;

    @BeforeEach
    void setUp() {
        identityApi = mock(IdentityApi.class);
        subjectRepository = mock(SubjectRepository.class);
        sectionRepository = mock(SectionRepository.class);
        topicRepository = mock(TopicRepository.class);
        topicSuggestionRepository = mock(TopicSuggestionRepository.class);
        mapper = new TaxonomyResponseMapper() {};

        service = new DefaultTaxonomyService(
                identityApi,
                subjectRepository,
                sectionRepository,
                topicRepository,
                topicSuggestionRepository,
                mapper
        );
    }

    @Test
    void suggestTopic_shouldCreateSuggestion() {
        Section section = Section.load(2L, 1L, "Backend", List.of());

        when(sectionRepository.findByIdAndSubjectId(2L, 1L))
                .thenReturn(section);
        when(identityApi.getCurrentUserId())
                .thenReturn(100L);
        when(topicSuggestionRepository.saveOrUpdate(any()))
                .thenAnswer(invocation -> {
                    TopicSuggestion suggestion = invocation.getArgument(0);
                    return TopicSuggestion.load(
                            10L,
                            suggestion.getCreatedByUserId(),
                            suggestion.getSubjectId(),
                            suggestion.getSectionId(),
                            suggestion.getName(),
                            suggestion.getDescription(),
                            suggestion.getStatus(),
                            suggestion.getModerationComment(),
                            suggestion.getResolvedTopicId()
                    );
                });

        TopicSuggestionResponse response = service.suggestTopic(
                1L,
                2L,
                new CreateTopicSuggestionRequest("Hibernate", "ORM")
        );

        assertEquals(10L, response.id());
        assertEquals(1L, response.subjectId());
        assertEquals(2L, response.sectionId());
        assertEquals("Hibernate", response.name());
        assertEquals("ORM", response.description());
        assertEquals(SuggestionsStatus.PENDING, response.status());

        verify(topicSuggestionRepository).saveOrUpdate(any());
    }

    @Test
    void suggestTopic_shouldThrowBusinessException_whenSectionNotFound() {
        when(sectionRepository.findByIdAndSubjectId(2L, 1L))
                .thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.suggestTopic(
                        1L,
                        2L,
                        new CreateTopicSuggestionRequest("Hibernate", "ORM")
                )
        );

        assertEquals("SECTION_NOT_FOUND", exception.getErrorCode().code());
        verifyNoInteractions(identityApi);
        verifyNoInteractions(topicSuggestionRepository);
    }

    @Test
    void getMyTopicSuggestions_shouldReturnCurrentUserSuggestions() {
        TopicSuggestion suggestion = TopicSuggestion.load(
                10L,
                100L,
                1L,
                2L,
                "Hibernate",
                "ORM",
                SuggestionsStatus.PENDING,
                null,
                null
        );

        when(identityApi.getCurrentUserId()).thenReturn(100L);
        when(topicSuggestionRepository.findAllByCreatedByUserId(100L))
                .thenReturn(List.of(suggestion));

        List<TopicSuggestionResponse> result = service.getMyTopicSuggestions();

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).id());
        assertEquals("Hibernate", result.get(0).name());
    }

    @Test
    void getSubjects_shouldReturnSubjects() {
        when(subjectRepository.findAll())
                .thenReturn(List.of(Subject.load(1L, "Java", List.of())));

        List<SubjectResponse> result = service.getSubjects();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("Java", result.get(0).name());
    }

    @Test
    void getSubject_shouldReturnSubjectDetails() {
        Subject subject = Subject.load(
                1L,
                "Java",
                List.of(Section.load(
                        2L,
                        1L,
                        "Backend",
                        List.of(Topic.load(
                                3L,
                                1L,
                                2L,
                                "Spring Framework",
                                "Spring")
                        )
                ))
        );

        when(subjectRepository.findByIdWithSectionsAndTopics(1L))
                .thenReturn(subject);

        SubjectDetailsResponse response = service.getSubject(1L);

        assertEquals(1L, response.id());
        assertEquals("Java", response.name());
        assertEquals(1, response.sections().size());
        assertEquals("Backend", response.sections().get(0).name());
        assertEquals("Spring", response.sections().get(0).topics().get(0).name());
    }

    @Test
    void getSubject_shouldThrowBusinessException_whenSubjectNotFound() {
        when(subjectRepository.findByIdWithSectionsAndTopics(1L))
                .thenReturn(null);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.getSubject(1L)
        );

        assertEquals("SUBJECT_NOT_FOUND", exception.getErrorCode().code());
    }

    @Test
    void getSection_shouldReturnSectionWithTopics() {
        Section section = Section.load(2L, 1L, "Backend", List.of());
        Topic topic = Topic.load(
                3L,
                1L,
                2L,
                "Spring Framework",
                "Spring"
        );

        when(sectionRepository.findByIdAndSubjectId(2L, 1L))
                .thenReturn(section);
        when(topicRepository.findAllBySectionId(2L))
                .thenReturn(List.of(topic));

        SectionDetailsResponse response = service.getSection(1L, 2L);

        assertEquals(2L, response.id());
        assertEquals(1L, response.subjectId());
        assertEquals("Backend", response.name());
        assertEquals(1, response.topics().size());
        assertEquals("Spring", response.topics().get(0).name());
    }

    @Test
    void getTopic_shouldReturnTopic() {
        Topic topic = Topic.load(
                3L,
                1L,
                2L,
                "Spring Framework",
                "Spring"
        );

        when(topicRepository.findByIdAndSectionIdAndSubjectId(3L, 2L, 1L))
                .thenReturn(topic);

        TopicDetailsResponse response = service.getTopic(1L, 2L, 3L);

        assertEquals(3L, response.id());
        assertEquals(1L, response.subjectId());
        assertEquals(2L, response.sectionId());
        assertEquals("Spring", response.name());
    }
}