package io.github.mirvmir.enrollment.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.mirvmir.common.exception.GlobalExceptionHandler;
import io.github.mirvmir.enrollment.application.service.interfaces.EnrollmentBookingService;
import io.github.mirvmir.enrollment.web.request.BookIndividualActivityRequest;
import io.github.mirvmir.enrollment.web.response.BookingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EnrollmentControllerTest {

    private EnrollmentBookingService enrollmentBookingService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        enrollmentBookingService = mock(EnrollmentBookingService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new EnrollmentController(enrollmentBookingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void bookCourse_shouldReturnCreated() throws Exception {
        BookingResponse response = new BookingResponse(
                1L,
                10L,
                "COURSE",
                100L,
                "Курс",
                new BigDecimal("3000"),
                Currency.getInstance("RUB"),
                "CREATED",
                "CREATED"
        );

        when(enrollmentBookingService.bookCourse(100L)).thenReturn(response);

        mockMvc.perform(post("/enrollments/courses/100/book"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1L))
                .andExpect(jsonPath("$.paymentId").value(10L))
                .andExpect(jsonPath("$.targetType").value("COURSE"));

        verify(enrollmentBookingService).bookCourse(100L);
    }

    @Test
    void bookGroupActivitySlot_shouldReturnCreated() throws Exception {
        BookingResponse response = new BookingResponse(
                1L,
                10L,
                "ACTIVITY_SLOT",
                200L,
                "Групповое занятие",
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                "CREATED",
                "CREATED"
        );

        when(enrollmentBookingService.bookGroupActivitySlot(200L)).thenReturn(response);

        mockMvc.perform(post("/enrollments/activity-slots/200/book"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.targetType").value("ACTIVITY_SLOT"))
                .andExpect(jsonPath("$.targetId").value(200L));

        verify(enrollmentBookingService).bookGroupActivitySlot(200L);
    }

    @Test
    void bookIndividualActivity_shouldReturnOk() throws Exception {
        Instant startAt = Instant.now().plusSeconds(3600);
        BookIndividualActivityRequest request =
                new BookIndividualActivityRequest(300L, startAt);

        BookingResponse response = new BookingResponse(
                1L,
                10L,
                "INDIVIDUAL_ACTIVITY",
                400L,
                "Индивидуальное занятие",
                new BigDecimal("2000"),
                Currency.getInstance("RUB"),
                "CREATED",
                "CREATED"
        );

        when(enrollmentBookingService.bookIndividualActivity(eq(400L), any()))
                .thenReturn(response);

        mockMvc.perform(post("/enrollments/individual-activities/400/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetType").value("INDIVIDUAL_ACTIVITY"))
                .andExpect(jsonPath("$.targetId").value(400L));

        verify(enrollmentBookingService).bookIndividualActivity(eq(400L), argThat(actual ->
                actual.activityTimeId().equals(300L)
                && actual.startAt().equals(startAt)
        ));
    }
}