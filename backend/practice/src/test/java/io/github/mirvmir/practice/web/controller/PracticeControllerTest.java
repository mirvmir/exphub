package io.github.mirvmir.practice.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.mirvmir.common.exception.GlobalExceptionHandler;
import io.github.mirvmir.practice.application.service.interfaces.PracticeService;
import io.github.mirvmir.practice.web.request.CreatePracticeAnswerRequest;
import io.github.mirvmir.practice.web.request.CreatePracticeCommentRequest;
import io.github.mirvmir.practice.web.response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PracticeControllerTest {

    private PracticeService practiceService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        practiceService = mock(PracticeService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new PracticeController(practiceService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void addAnswerByStudent_shouldReturnAnswer() throws Exception {
        CreatePracticeAnswerRequest request =
                new CreatePracticeAnswerRequest("<p>Ответ студента</p>", 50L);

        Instant createdAt = Instant.parse("2026-05-13T10:00:00Z");

        PracticeAnswerResponse response = new PracticeAnswerResponse(
                1L,
                10L,
                "<p>Ответ студента</p>",
                50L,
                createdAt
        );

        when(practiceService.addAnswer(eq(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6")),
                any()))
                .thenReturn(response);

        mockMvc.perform(post("/student/practice/lessons/3fa85f64-5717-4562-b3fc-2c963f66afa6/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.practiceSubmissionId").value(10L))
                .andExpect(jsonPath("$.html").value("<p>Ответ студента</p>"))
                .andExpect(jsonPath("$.fileId").value(50L));

        verify(practiceService).addAnswer(eq(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6")),
                argThat(actual ->
                        actual.html().equals("<p>Ответ студента</p>")
                                && actual.fileId().equals(50L)
                )
        );
    }

    @Test
    void addCommentByTeacher_shouldReturnComment() throws Exception {
        CreatePracticeCommentRequest request =
                new CreatePracticeCommentRequest("<p>Комментарий преподавателя</p>", 60L);

        Instant createdAt = Instant.parse("2026-05-13T11:00:00Z");

        PracticeCommentResponse response = new PracticeCommentResponse(
                2L,
                1L,
                "<p>Комментарий преподавателя</p>",
                60L,
                createdAt
        );

        when(practiceService.addComment(eq(1L), any()))
                .thenReturn(response);

        mockMvc.perform(post("/author/practice/answers/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.practiceSubmissionAnswerId").value(1L))
                .andExpect(jsonPath("$.html").value("<p>Комментарий преподавателя</p>"))
                .andExpect(jsonPath("$.fileId").value(60L));
    }

    @Test
    void checkSubmissionByTeacher_shouldReturnSubmission() throws Exception {
        PracticeSubmissionResponse response = new PracticeSubmissionResponse(
                10L,
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                200L,
                1L,
                Instant.parse("2026-05-13T09:00:00Z"),
                Instant.parse("2026-05-13T12:00:00Z")
        );

        when(practiceService.checkSubmissionByTeacher(10L))
                .thenReturn(response);

        mockMvc.perform(post("/author/practice/submissions/10/check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.stableLessonId").value("3fa85f64-5717-4562-b3fc-2c963f66afa6"))
                .andExpect(jsonPath("$.courseEnrollmentId").value(200L))
                .andExpect(jsonPath("$.studentId").value(1L));
    }

    @Test
    void getMySubmission_shouldReturnSubmissionDetails() throws Exception {
        PracticeSubmissionDetailsResponse response =
                submissionDetailsResponse();

        when(practiceService.getMySubmission(
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6")))
                .thenReturn(response);

        mockMvc.perform(get("/student/practice/lessons/3fa85f64-5717-4562-b3fc-2c963f66afa6/submission"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.stableLessonId").value("3fa85f64-5717-4562-b3fc-2c963f66afa6"))
                .andExpect(jsonPath("$.courseEnrollmentId").value(200L))
                .andExpect(jsonPath("$.studentId").value(1L))
                .andExpect(jsonPath("$.answers[0].id").value(1000L))
                .andExpect(jsonPath("$.answers[0].comments[0].id").value(5000L));

        verify(practiceService).getMySubmission(
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6")
        );
    }

    @Test
    void getLessonSubmissionsForTeacher_shouldReturnSubmissions() throws Exception {
        PracticeSubmissionDetailsResponse response =
                submissionDetailsResponse();

        when(practiceService.getLessonSubmissionsForTeacher(
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6")))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/teacher/practice/lessons/3fa85f64-5717-4562-b3fc-2c963f66afa6/submissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].stableLessonId").value("3fa85f64-5717-4562-b3fc-2c963f66afa6"))
                .andExpect(jsonPath("$[0].studentId").value(1L))
                .andExpect(jsonPath("$[0].answers[0].id").value(1000L))
                .andExpect(jsonPath("$[0].answers[0].comments[0].id").value(5000L));

        verify(practiceService).getLessonSubmissionsForTeacher(
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6")
        );
    }

    private PracticeSubmissionDetailsResponse submissionDetailsResponse() {
        return new PracticeSubmissionDetailsResponse(
                10L,
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                200L,
                1L,
                Instant.parse("2026-05-13T09:00:00Z"),
                null,
                List.of(
                        new PracticeAnswerDetailsResponse(
                                1000L,
                                10L,
                                "<p>Ответ</p>",
                                50L,
                                Instant.parse("2026-05-13T09:10:00Z"),
                                List.of(
                                        new PracticeCommentResponse(
                                                5000L,
                                                1000L,
                                                "<p>Комментарий</p>",
                                                60L,
                                                Instant.parse("2026-05-13T09:20:00Z")
                                        )
                                )
                        )
                )
        );
    }
}