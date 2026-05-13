package io.github.mirvmir.taxonomy.web.controller;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.GlobalExceptionHandler;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.taxonomy.application.service.interfaces.TaxonomyService;
import io.github.mirvmir.taxonomy.domain.SuggestionsStatus;
import io.github.mirvmir.taxonomy.exception.TaxonomyErrorCode;
import io.github.mirvmir.taxonomy.web.response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaxonomyControllerTest {

    private TaxonomyService taxonomyService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        taxonomyService = mock(TaxonomyService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TaxonomyController(taxonomyService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getSubjects_shouldReturn200() throws Exception {
        when(taxonomyService.getSubjects())
                .thenReturn(List.of(new SubjectResponse(1L, "Java")));

        mockMvc.perform(get("/taxonomy/subjects"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Java"));

        verify(taxonomyService).getSubjects();
    }

    @Test
    void getSubject_shouldReturn404_whenSubjectNotFound() throws Exception {
        when(taxonomyService.getSubject(1L))
                .thenThrow(new NotFoundException(TaxonomyErrorCode.SUBJECT_NOT_FOUND));

        mockMvc.perform(get("/taxonomy/subjects/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SUBJECT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Subject not found"));
    }

    @Test
    void getSection_shouldReturn200() throws Exception {
        SectionDetailsResponse response = new SectionDetailsResponse(
                2L,
                1L,
                "Backend",
                List.of(new TopicShortResponse(3L, "Spring"))
        );

        when(taxonomyService.getSection(1L, 2L))
                .thenReturn(response);

        mockMvc.perform(get("/taxonomy/subjects/1/sections/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.subjectId").value(1L))
                .andExpect(jsonPath("$.name").value("Backend"))
                .andExpect(jsonPath("$.topics[0].id").value(3L))
                .andExpect(jsonPath("$.topics[0].name").value("Spring"));
    }

    @Test
    void getTopic_shouldReturn200() throws Exception {
        TopicDetailsResponse response = new TopicDetailsResponse(
                3L,
                1L,
                2L,
                "Spring",
                "Spring Framework"
        );

        when(taxonomyService.getTopic(1L, 2L, 3L))
                .thenReturn(response);

        mockMvc.perform(get("/taxonomy/subjects/1/sections/2/topics/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.name").value("Spring"))
                .andExpect(jsonPath("$.description").value("Spring Framework"));
    }

    @Test
    void suggestTopic_shouldReturn200() throws Exception {
        TopicSuggestionResponse response = new TopicSuggestionResponse(
                10L,
                1L,
                2L,
                "Hibernate",
                "ORM",
                SuggestionsStatus.PENDING
        );

        when(taxonomyService.suggestTopic(eq(1L), eq(2L), any()))
                .thenReturn(response);

        mockMvc.perform(post("/taxonomy/subjects/1/sections/2/topics/suggestions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Hibernate",
                                  "description": "ORM"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("Hibernate"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(taxonomyService).suggestTopic(eq(1L), eq(2L), any());
    }

    @Test
    void getMyTopicSuggestions_shouldReturn200() throws Exception {
        when(taxonomyService.getMyTopicSuggestions())
                .thenReturn(List.of(new TopicSuggestionResponse(
                        10L,
                        1L,
                        2L,
                        "Hibernate",
                        "ORM",
                        SuggestionsStatus.PENDING
                )));

        mockMvc.perform(get("/taxonomy/topics/suggestions/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }
}