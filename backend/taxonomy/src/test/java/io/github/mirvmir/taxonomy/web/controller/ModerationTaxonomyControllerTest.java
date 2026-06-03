package io.github.mirvmir.taxonomy.web.controller;

import io.github.mirvmir.common.exception.GlobalExceptionHandler;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.taxonomy.application.service.interfaces.TaxonomyModerationService;
import io.github.mirvmir.taxonomy.domain.SuggestionsStatus;
import io.github.mirvmir.taxonomy.exception.TaxonomyErrorCode;
import io.github.mirvmir.taxonomy.web.response.SectionDetailsResponse;
import io.github.mirvmir.taxonomy.web.response.SubjectResponse;
import io.github.mirvmir.taxonomy.web.response.TopicDetailsResponse;
import io.github.mirvmir.taxonomy.web.response.TopicSuggestionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ModerationTaxonomyControllerTest {

    private TaxonomyModerationService taxonomyModerationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        taxonomyModerationService = mock(TaxonomyModerationService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ModerationTaxonomyController(taxonomyModerationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getTopicSuggestions_shouldReturn200() throws Exception {
        when(taxonomyModerationService.getPendingSuggestions())
                .thenReturn(List.of(new TopicSuggestionResponse(
                        1L,
                        10L,
                        20L,
                        "Hibernate",
                        "ORM",
                        SuggestionsStatus.PENDING
                )));

        mockMvc.perform(get("/moderation/taxonomy/topics/suggestions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void approveTopicSuggestion_shouldReturn200() throws Exception {
        when(taxonomyModerationService.approveSuggestion(1L))
                .thenReturn(new TopicDetailsResponse(
                        100L,
                        10L,
                        20L,
                        "Hibernate",
                        "ORM"
                ));

        mockMvc.perform(post("/moderation/taxonomy/topics/suggestions/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.name").value("Hibernate"));
    }

    @Test
    void approveTopicSuggestion_shouldReturn404_whenSuggestionNotFound() throws Exception {
        when(taxonomyModerationService.approveSuggestion(1L))
                .thenThrow(new NotFoundException(TaxonomyErrorCode.TOPIC_SUGGESTION_NOT_FOUND));

        mockMvc.perform(post("/moderation/taxonomy/topics/suggestions/1/approve"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TOPIC_SUGGESTION_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Topic suggestion not found"));
    }

    @Test
    void mergeTopicSuggestion_shouldReturn200() throws Exception {
        when(taxonomyModerationService.mergeSuggestion(eq(1L), any()))
                .thenReturn(new TopicSuggestionResponse(
                        1L,
                        10L,
                        20L,
                        "Hibernate",
                        "ORM",
                        SuggestionsStatus.MERGED
                ));

        mockMvc.perform(post("/moderation/taxonomy/topics/suggestions/1/merge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "resolvedTopicId": 100
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("MERGED"));
    }

    @Test
    void rejectTopicSuggestion_shouldReturn200() throws Exception {
        when(taxonomyModerationService.rejectSuggestion(eq(1L), any()))
                .thenReturn(new TopicSuggestionResponse(
                        1L,
                        10L,
                        20L,
                        "Hibernate",
                        "ORM",
                        SuggestionsStatus.REJECTED
                ));

        mockMvc.perform(post("/moderation/taxonomy/topics/suggestions/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "moderationComment": "Такая тема уже есть"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void createSubject_shouldReturn200() throws Exception {
        when(taxonomyModerationService.createSubject(any()))
                .thenReturn(new SubjectResponse(1L, "Java"));

        mockMvc.perform(post("/moderation/taxonomy/subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Java"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Java"));
    }

    @Test
    void createSection_shouldReturn200() throws Exception {
        when(taxonomyModerationService.createSection(eq(1L), any()))
                .thenReturn(new SectionDetailsResponse(
                        2L,
                        1L,
                        "Backend",
                        List.of()
                ));

        mockMvc.perform(post("/moderation/taxonomy/subjects/1/sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Backend"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.subjectId").value(1L))
                .andExpect(jsonPath("$.name").value("Backend"));
    }

    @Test
    void createTopic_shouldReturn200() throws Exception {
        when(taxonomyModerationService.createTopic(eq(1L), eq(2L), any()))
                .thenReturn(new TopicDetailsResponse(
                        3L,
                        1L,
                        2L,
                        "Spring",
                        "Spring Framework"
                ));

        mockMvc.perform(post("/moderation/taxonomy/subjects/1/sections/2/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Spring",
                                  "description": "Spring Framework"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.name").value("Spring"));
    }
}