package io.github.mirvmir.enrollment.web.controller;

import io.github.mirvmir.common.exception.GlobalExceptionHandler;
import io.github.mirvmir.enrollment.application.service.interfaces.CourseProgressService;
import io.github.mirvmir.enrollment.web.response.CourseProgressResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StudentCourseProgressControllerTest {

    private CourseProgressService courseProgressService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        courseProgressService = mock(CourseProgressService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new StudentCourseProgressController(courseProgressService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void completeLesson_shouldReturnCourseProgress() throws Exception {
        CourseProgressResponse response = new CourseProgressResponse(
                1L,
                100L,
                200L,
                300L,
                BigDecimal.valueOf(100),
                new BigDecimal("50.00"),
                new BigDecimal("25.00"),
                "PAYED"
        );

        when(courseProgressService.completeLesson(100L, 300L))
                .thenReturn(response);

        mockMvc.perform(post("/student/courses/100/lessons/300/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseEnrollmentId").value(1L))
                .andExpect(jsonPath("$.courseId").value(100L))
                .andExpect(jsonPath("$.courseModuleId").value(200L))
                .andExpect(jsonPath("$.courseLessonId").value(300L))
                .andExpect(jsonPath("$.enrollmentStatus").value("PAYED"));

        verify(courseProgressService).completeLesson(100L, 300L);
    }
}
