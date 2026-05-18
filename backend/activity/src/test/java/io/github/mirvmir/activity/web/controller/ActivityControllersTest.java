package io.github.mirvmir.activity.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.mirvmir.activity.application.service.interfaces.ActivityService;
import io.github.mirvmir.activity.application.service.interfaces.ActivitySlotService;
import io.github.mirvmir.activity.application.service.interfaces.AuthorActivityService;
import io.github.mirvmir.activity.application.service.interfaces.AuthorActivityTimeService;
import io.github.mirvmir.activity.application.service.interfaces.ModerationActivityService;
import io.github.mirvmir.activity.domain.ActivityType;
import io.github.mirvmir.activity.exception.ActivityErrorCode;
import io.github.mirvmir.activity.web.request.CancelActivitySlotRequest;
import io.github.mirvmir.activity.web.request.CreateActivityRequest;
import io.github.mirvmir.activity.web.request.CreateAvailabilityTimeRequest;
import io.github.mirvmir.activity.web.request.CreateGroupActivitySlotRequest;
import io.github.mirvmir.activity.web.request.RejectActivityRequest;
import io.github.mirvmir.activity.web.request.UpdateActivityRequest;
import io.github.mirvmir.activity.web.request.UpdateActivitySlotRoomJoinUrlRequest;
import io.github.mirvmir.activity.web.response.ActivityDescriptionResponse;
import io.github.mirvmir.activity.web.response.ActivityResponse;
import io.github.mirvmir.activity.web.response.ActivitySlotResponse;
import io.github.mirvmir.activity.web.response.ActivityTimeResponse;
import io.github.mirvmir.activity.web.response.IdResponse;
import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.GlobalExceptionHandler;
import io.github.mirvmir.common.exception.ForbiddenException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.payment.exception.PaymentUnavailableException;
import io.github.mirvmir.payment.web.handler.PaymentExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.Set;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class ActivityControllersTest {

    private ActivityService activityService;
    private AuthorActivityService authorActivityService;
    private ActivitySlotService activitySlotService;
    private AuthorActivityTimeService authorActivityTimeService;
    private ModerationActivityService moderationActivityService;

    private MockMvc activityMockMvc;
    private MockMvc authorMockMvc;
    private MockMvc studentMockMvc;
    private MockMvc moderationMockMvc;
    private ObjectMapper objectMapper;

    private final Clock clock = Clock.fixed(Instant.parse("2026-05-12T10:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        activityService = mock(ActivityService.class);
        authorActivityService = mock(AuthorActivityService.class);
        activitySlotService = mock(ActivitySlotService.class);
        authorActivityTimeService = mock(AuthorActivityTimeService.class);
        moderationActivityService = mock(ModerationActivityService.class);

        activityMockMvc = MockMvcBuilders
                .standaloneSetup(new ActivityController(activityService))
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .build();
        authorMockMvc = MockMvcBuilders
                .standaloneSetup(new AuthorActivityController(
                        authorActivityService,
                        activitySlotService,
                        authorActivityTimeService
                ))
                .setControllerAdvice(
                        new GlobalExceptionHandler(),
                        new PaymentExceptionHandler()
                )
                .build();
        studentMockMvc = MockMvcBuilders
                .standaloneSetup(new StudentActivityController(activitySlotService))
                .setControllerAdvice(
                        new GlobalExceptionHandler(),
                        new PaymentExceptionHandler()
                )
                .build();
        moderationMockMvc = MockMvcBuilders
                .standaloneSetup(new ModerationActivityController(moderationActivityService))
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .build()
        ;

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void getActivityById_shouldReturn200() throws Exception {
        ActivityDescriptionResponse response = new ActivityDescriptionResponse(
                1L,
                2L,
                null,
                "Занятие",
                "Кратко",
                "<p>Описание</p>",
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                60,
                ActivityType.INDIVIDUAL,
                7L,
                Set.of(11L),
                false,
                Set.of(),
                Set.of()
        );

        when(activityService.getActivity(1L)).thenReturn(response);

        activityMockMvc.perform(get("/activities/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(activityService).getActivity(1L);
    }

    @Test
    void createActivity_shouldReturn201() throws Exception {
        CreateActivityRequest request = new CreateActivityRequest(
                "Занятие",
                "Кратко",
                "<p>Описание</p>",
                5,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                60,
                ActivityType.GROUP,
                null
        );

        when(authorActivityService.createActivity(any())).thenReturn(new IdResponse(1L));

        authorMockMvc.perform(post("/author/activities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(authorActivityService).createActivity(argThat(actual ->
                actual != null
                        && actual.title().equals("Занятие")
                        && actual.type() == ActivityType.GROUP
                        && actual.maxBookableSeats().equals(5)));
    }

    @Test
    void updateActivity_shouldReturn200() throws Exception {
        UpdateActivityRequest request = new UpdateActivityRequest(
                "Новое занятие",
                "Новое кратко",
                "<p>Новое описание</p>",
                4,
                new BigDecimal("2000"),
                Currency.getInstance("RUB"),
                90
        );
        ActivityResponse response = new ActivityResponse(
                1L,
                request.title(),
                request.shortDescription(),
                request.descriptionHtml(),
                request.priceAmount(),
                request.priceCurrency(),
                request.durationMinutes(),
                ActivityType.GROUP
        );

        when(authorActivityService.updateActivity(eq(1L), any())).thenReturn(response);

        authorMockMvc.perform(patch("/author/activities/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(authorActivityService).updateActivity(eq(1L), argThat(actual ->
                actual != null
                        && actual.title().equals("Новое занятие")
                        && actual.maxBookableSeats().equals(4)
        ));
    }

    @Test
    void publish_shouldReturn204() throws Exception {
        authorMockMvc.perform(post("/author/activities/1/publish"))
                .andExpect(status().isNoContent());

        verify(authorActivityService).publish(1L);
    }

    @Test
    void archive_shouldReturn204() throws Exception {
        authorMockMvc.perform(post("/author/activities/1/archive"))
                .andExpect(status().isNoContent());

        verify(authorActivityService).archive(1L);
    }

    @Test
    void unarchive_shouldReturn204() throws Exception {
        authorMockMvc.perform(post("/author/activities/1/unarchive"))
                .andExpect(status().isNoContent());

        verify(authorActivityService).unarchive(1L);
    }

    @Test
    void deleteActivity_shouldReturn204() throws Exception {
        authorMockMvc.perform(delete("/author/activities/1"))
                .andExpect(status().isNoContent());

        verify(authorActivityService).deleteActivity(1L);
    }

    @Test
    void updateSlotRoomJoinUrl_shouldReturn204() throws Exception {
        UpdateActivitySlotRoomJoinUrlRequest request = new UpdateActivitySlotRoomJoinUrlRequest("https://meet.test/room");

        authorMockMvc.perform(patch("/author/activities/slots/10/room-join-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(activitySlotService).updateRoomJoinUrl(eq(10L), argThat(actual ->
                actual != null && actual.roomJoinUrl().equals("https://meet.test/room")
        ));
    }

    @Test
    void cancelSlotByAuthor_shouldReturn204() throws Exception {
        CancelActivitySlotRequest request = new CancelActivitySlotRequest("Не состоится");

        authorMockMvc.perform(post("/author/activities/10/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(activitySlotService).cancelByAuthor(eq(10L), argThat(actual ->
                actual != null && actual.reason().equals("Не состоится")
        ));
    }

    @Test
    void completeSlot_shouldReturn204() throws Exception {
        authorMockMvc.perform(post("/author/activities/10/complete"))
                .andExpect(status().isNoContent());

        verify(activitySlotService).complete(10L);
    }

    @Test
    void createGroupSlot_shouldReturn200() throws Exception {
        Instant startAt = Instant.now().plusSeconds(3600);
        CreateGroupActivitySlotRequest request = new CreateGroupActivitySlotRequest(startAt);
        ActivitySlotResponse response = new ActivitySlotResponse(
                10L,
                1L,
                startAt,
                startAt.plusSeconds(3600)
        );

        when(authorActivityService.createGroupSlot(eq(1L), any())).thenReturn(response);

        authorMockMvc.perform(post("/author/activities/1/slots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(authorActivityService).createGroupSlot(eq(1L), argThat(actual ->
                actual != null && actual.startTime().equals(startAt)
        ));
    }

    @Test
    void createAvailabilityTime_shouldReturn200() throws Exception {
        Instant startAt = Instant.now().plusSeconds(3600);
        Instant endAt = Instant.now().plusSeconds(10800);
        CreateAvailabilityTimeRequest request = new CreateAvailabilityTimeRequest(startAt, endAt);
        ActivityTimeResponse response = new ActivityTimeResponse(
                1L,
                startAt,
                startAt.plusSeconds(3600)
        );

        when(authorActivityTimeService.createAvailabilityTime(eq(1L), any())).thenReturn(response);

        authorMockMvc.perform(post("/author/activities/1/availability-times")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(authorActivityTimeService).createAvailabilityTime(eq(1L), argThat(actual ->
                actual != null && actual.startAt().equals(startAt)
        ));
    }

    @Test
    void cancelSlotByStudent_shouldReturn204() throws Exception {
        CancelActivitySlotRequest request = new CancelActivitySlotRequest("Не смогу прийти");

        studentMockMvc.perform(post("/student/activities/10/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(activitySlotService).cancelByStudent(eq(10L), argThat(actual ->
                actual != null && actual.reason().equals("Не смогу прийти")
        ));
    }

    @Test
    void approve_shouldReturn204() throws Exception {
        moderationMockMvc.perform(post("/moderation/activities/1/approve"))
                .andExpect(status().isNoContent());

        verify(moderationActivityService).approve(1L);
    }

    @Test
    void reject_shouldReturn204() throws Exception {
        RejectActivityRequest request = new RejectActivityRequest("Нужно исправить описание");

        moderationMockMvc.perform(post("/moderation/activities/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(moderationActivityService).reject(eq(1L), argThat(actual ->
                actual != null && actual.moderationComment().equals("Нужно исправить описание")
        ));
    }

    @Test
    void block_shouldReturn204() throws Exception {
        moderationMockMvc.perform(post("/moderation/activities/1/block"))
                .andExpect(status().isNoContent());

        verify(moderationActivityService).block(1L);
    }

    @Test
    void getActivityById_whenActivityNotFound_shouldReturn404() throws Exception {
        when(activityService.getActivity(1L))
                .thenThrow(new NotFoundException(
                        ActivityErrorCode.ACTIVITY_NOT_FOUND,
                        "Activity with id=1 not found"
                ));

        activityMockMvc.perform(get("/activities/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ACTIVITY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Activity with id=1 not found"));
    }

    @Test
    void updateActivity_whenForbidden_shouldReturn403() throws Exception {
        UpdateActivityRequest request = new UpdateActivityRequest(
                "Новое занятие",
                "Новое кратко",
                "<p>Новое описание</p>",
                4,
                new BigDecimal("2000"),
                Currency.getInstance("RUB"),
                90
        );

        when(authorActivityService.updateActivity(eq(1L), any()))
                .thenThrow(new ForbiddenException(ActivityErrorCode.ACTIVITY_FORBIDDEN));

        authorMockMvc.perform(patch("/author/activities/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACTIVITY_FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("You are not allowed to manage this activity"));
    }

    @Test
    void publish_whenBusinessError_shouldReturn409() throws Exception {
        doThrow(new BusinessException(ActivityErrorCode.ACTIVITY_NOT_DRAFT))
                .when(authorActivityService).publish(1L);

        authorMockMvc.perform(post("/author/activities/1/publish"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ACTIVITY_NOT_DRAFT"))
                .andExpect(jsonPath("$.message").value("Only draft activities can be modified"));
    }

    @Test
    void cancelSlotByStudent_whenReasonIsBlank_shouldReturn400() throws Exception {
        studentMockMvc.perform(post("/student/activities/10/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "reason": ""
                            }
                            """))
                .andExpect(status().isBadRequest());

        verify(activitySlotService, never()).cancelByStudent(anyLong(), any());
    }

    @Test
    void getActivityById_whenDatabaseUnavailable_shouldReturn503() throws Exception {
        when(activityService.getActivity(1L))
                .thenThrow(new CannotGetJdbcConnectionException("db unavailable"));

        activityMockMvc.perform(get("/activities/1"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("DATABASE_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("Временная ошибка подключения к базе данных"));
    }

    @Test
    void getActivityById_whenDataAccessError_shouldReturn500() throws Exception {
        when(activityService.getActivity(1L))
                .thenThrow(new DataAccessResourceFailureException("db error"));

        activityMockMvc.perform(get("/activities/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("DATABASE_ERROR"))
                .andExpect(jsonPath("$.message").value("Ошибка при обращении к базе данных"));
    }

    @Test
    void cancelSlotByStudent_whenPaymentUnavailable_shouldReturn503() throws Exception {
        CancelActivitySlotRequest request = new CancelActivitySlotRequest("Не смогу прийти");

        doThrow(new PaymentUnavailableException("Платежная система временно недоступна"))
                .when(activitySlotService).cancelByStudent(eq(10L), any());

        studentMockMvc.perform(post("/student/activities/10/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Платежная система временно недоступна"));
    }

}
