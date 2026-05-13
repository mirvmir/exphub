package io.github.mirvmir.review.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.ForbiddenException;
import io.github.mirvmir.common.exception.GlobalExceptionHandler;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.review.application.service.interfaces.ReviewModerationService;
import io.github.mirvmir.review.application.service.interfaces.ReviewService;
import io.github.mirvmir.review.domain.ReviewStatus;
import io.github.mirvmir.review.domain.ReviewTargetType;
import io.github.mirvmir.review.exception.ReviewErrorCode;
import io.github.mirvmir.review.web.request.CreateReviewRequest;
import io.github.mirvmir.review.web.response.ReviewResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReviewControllersTest {

    private ReviewService reviewService;
    private ReviewModerationService reviewModerationService;

    private MockMvc reviewMockMvc;
    private MockMvc moderationMockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reviewService = mock(ReviewService.class);
        reviewModerationService = mock(ReviewModerationService.class);

        reviewMockMvc = MockMvcBuilders
                .standaloneSetup(new ReviewController(reviewService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        moderationMockMvc = MockMvcBuilders
                .standaloneSetup(new ReviewModerationController(reviewModerationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void createActivityReview_shouldReturn200() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest(
                "Хорошее занятие",
                5.0
        );
        ReviewResponse response = new ReviewResponse(
                1L,
                request.comment(),
                request.score(),
                10L,
                2L,
                ReviewTargetType.ACTIVITY,
                ReviewStatus.MODERATION
        );

        when(reviewService.createActivityReview(eq(10L), any())).thenReturn(response);

        reviewMockMvc.perform(post("/reviews/activities/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.comment").value("Хорошее занятие"))
                .andExpect(jsonPath("$.score").value(5.0))
                .andExpect(jsonPath("$.targetType").value("ACTIVITY"))
                .andExpect(jsonPath("$.status").value("MODERATION"));

        verify(reviewService).createActivityReview(eq(10L), argThat(actual ->
                actual != null
                        && actual.comment().equals("Хорошее занятие")
                        && actual.score().equals(5.0)
        ));
    }

    @Test
    void createCourseReview_shouldReturn200() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest(
                "Хороший курс",
                4.0
        );
        ReviewResponse response = new ReviewResponse(
                2L,
                request.comment(),
                request.score(),
                20L,
                3L,
                ReviewTargetType.COURSE,
                ReviewStatus.MODERATION
        );

        when(reviewService.createCourseReview(eq(20L), any())).thenReturn(response);

        reviewMockMvc.perform(post("/reviews/courses/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.comment").value("Хороший курс"))
                .andExpect(jsonPath("$.score").value(4.0))
                .andExpect(jsonPath("$.targetType").value("COURSE"))
                .andExpect(jsonPath("$.status").value("MODERATION"));

        verify(reviewService).createCourseReview(eq(20L), argThat(actual ->
                actual != null
                        && actual.comment().equals("Хороший курс")
                        && actual.score().equals(4.0)
        ));
    }

    @Test
    void approveReview_shouldReturn200() throws Exception {
        ReviewResponse response = new ReviewResponse(
                1L,
                "Хорошее занятие",
                5.0,
                10L,
                2L,
                ReviewTargetType.ACTIVITY,
                ReviewStatus.PUBLISHED
        );

        when(reviewModerationService.approveReview(1L)).thenReturn(response);

        moderationMockMvc.perform(patch("/admin/reviews/1/approve"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(reviewModerationService).approveReview(1L);
    }

    @Test
    void rejectReview_shouldReturn200() throws Exception {
        ReviewResponse response = new ReviewResponse(
                1L,
                "Хорошее занятие",
                5.0,
                10L,
                2L,
                ReviewTargetType.ACTIVITY,
                ReviewStatus.REJECTED
        );

        when(reviewModerationService.rejectReview(1L)).thenReturn(response);

        moderationMockMvc.perform(patch("/admin/reviews/1/reject"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(reviewModerationService).rejectReview(1L);
    }

    @Test
    void createActivityReview_whenForbidden_shouldReturn403() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest(
                "Хорошее занятие",
                5.0
        );

        when(reviewService.createActivityReview(eq(10L), any()))
                .thenThrow(new ForbiddenException(
                        ReviewErrorCode.USER_IS_NOT_ACTIVITY_STUDENT,
                        "Пользователь не является студентом данного занятия"
                ));

        reviewMockMvc.perform(post("/reviews/activities/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("USER_IS_NOT_ACTIVITY_STUDENT"))
                .andExpect(jsonPath("$.message").value("Пользователь не является студентом данного занятия"));
    }

    @Test
    void createCourseReview_whenReviewAlreadyExists_shouldReturn409() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest(
                "Хороший курс",
                4.0
        );

        when(reviewService.createCourseReview(eq(20L), any()))
                .thenThrow(new BusinessException(ReviewErrorCode.REVIEW_ALREADY_EXISTS));

        reviewMockMvc.perform(post("/reviews/courses/20")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REVIEW_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("У пользователя уже есть отзыв"));
    }

    @Test
    void approveReview_whenReviewNotFound_shouldReturn404() throws Exception {
        when(reviewModerationService.approveReview(1L))
                .thenThrow(new NotFoundException(
                        ReviewErrorCode.REVIEW_NOT_FOUND,
                        "Отзыв с id=1 не найден"
                ));

        moderationMockMvc.perform(patch("/admin/reviews/1/approve"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("REVIEW_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Отзыв с id=1 не найден"));
    }

    @Test
    void createActivityReview_whenDatabaseUnavailable_shouldReturn503() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest(
                "Хорошее занятие",
                5.0
        );

        when(reviewService.createActivityReview(eq(10L), any()))
                .thenThrow(new CannotGetJdbcConnectionException("db unavailable"));

        reviewMockMvc.perform(post("/reviews/activities/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("DATABASE_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("Временная ошибка подключения к базе данных"));
    }

    @Test
    void createActivityReview_whenDataAccessError_shouldReturn500() throws Exception {
        CreateReviewRequest request = new CreateReviewRequest(
                "Хорошее занятие",
                5.0
        );

        when(reviewService.createActivityReview(eq(10L), any()))
                .thenThrow(new DataAccessResourceFailureException("db error"));

        reviewMockMvc.perform(post("/reviews/activities/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("DATABASE_ERROR"))
                .andExpect(jsonPath("$.message").value("Ошибка при обращении к базе данных"));
    }
}
