package io.github.mirvmir.activity.application.service.implementation;

import io.github.mirvmir.activity.api.event.ActivityDeleteEvent;
import io.github.mirvmir.activity.application.persistence.mapper.ActivityEventMapper;
import io.github.mirvmir.activity.application.service.port.event.ActivityEventPublisher;
import io.github.mirvmir.activity.application.service.port.repository.ActivityRepository;
import io.github.mirvmir.activity.application.service.port.repository.ActivitySlotRepository;
import io.github.mirvmir.activity.domain.Activity;
import io.github.mirvmir.activity.domain.ActivitySlot;
import io.github.mirvmir.activity.domain.ActivitySlotStatus;
import io.github.mirvmir.activity.domain.ActivityType;
import io.github.mirvmir.activity.exception.ActivityErrorCode;
import io.github.mirvmir.activity.web.request.RejectActivityRequest;
import io.github.mirvmir.common.domain.ContentStatus;
import io.github.mirvmir.common.domain.ModerationStatus;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultModerationActivityServiceTest {

    private EnrollmentApi enrollmentApi;
    private ActivityRepository activityRepository;
    private ActivitySlotRepository activitySlotRepository;
    private ActivityEventPublisher activityEventPublisher;
    private ActivityEventMapper activityEventMapper;

    private DefaultModerationActivityService service;

    @BeforeEach
    void setUp() {
        enrollmentApi = mock(EnrollmentApi.class);
        activityRepository = mock(ActivityRepository.class);
        activitySlotRepository = mock(ActivitySlotRepository.class);
        activityEventPublisher = mock(ActivityEventPublisher.class);
        activityEventMapper = mock(ActivityEventMapper.class);

        service = new DefaultModerationActivityService(
                enrollmentApi,
                activityRepository,
                activitySlotRepository,
                activityEventPublisher,
                activityEventMapper
        );
    }

    @Test
    void approve_shouldApproveActivityAndPublishEvent() {
        Activity activity = activity(ContentStatus.DRAFT, ModerationStatus.PENDING, null);

        when(activityRepository.findById(activity.getId())).thenReturn(activity);
        when(activityEventMapper.toPublishedEvent(activity)).thenReturn(null);

        service.approve(activity.getId());

        assertEquals(ContentStatus.ACTIVE, activity.getContentStatus());
        assertEquals(ModerationStatus.APPROVED, activity.getModerationStatus());
        verify(activityRepository).saveOrUpdate(activity);
        verify(activityEventPublisher).publish(null);
    }

    @Test
    void reject_shouldRejectActivityWithModerationComment() {
        Activity activity = activity(ContentStatus.DRAFT, ModerationStatus.PENDING, null);
        RejectActivityRequest request = new RejectActivityRequest("Нужно исправить описание");

        when(activityRepository.findById(activity.getId())).thenReturn(activity);

        service.reject(activity.getId(), request);

        assertEquals(ModerationStatus.REJECTED, activity.getModerationStatus());
        assertEquals(request.moderationComment(), activity.getModerationComment());
        verify(activityRepository).saveOrUpdate(activity);
        verifyNoInteractions(activityEventPublisher);
    }

    @Test
    void block_shouldBlockActivityRefundEnrollmentsAndDeleteEvent() {
        Activity activity = activity(ContentStatus.ACTIVE, ModerationStatus.APPROVED, null);
        ActivitySlot slot1 = slot(10L, activity.getId());
        ActivitySlot slot2 = slot(11L, activity.getId());

        when(activityRepository.findById(activity.getId())).thenReturn(activity);
        when(activitySlotRepository.findByActivityId(activity.getId())).thenReturn(List.of(slot1, slot2));

        service.block(activity.getId());

        assertEquals(ContentStatus.BLOCKED, activity.getContentStatus());
        verify(activityRepository).saveOrUpdate(activity);
        verify(enrollmentApi).refundPayedActivityEnrollments(slot1.getId(), "Занятие заблокировано");
        verify(enrollmentApi).refundPayedActivityEnrollments(slot2.getId(), "Занятие заблокировано");
        verify(activityEventPublisher).delete(any(ActivityDeleteEvent.class));
    }

    @Test
    void approve_shouldThrowNotFound_whenActivityDoesNotExist() {
        when(activityRepository.findById(1L)).thenReturn(null);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.approve(1L));
        verify(activityRepository, never()).saveOrUpdate(any());
    }

    private Activity activity(ContentStatus contentStatus,
                              ModerationStatus moderationStatus,
                              String moderationComment) {
        return Activity.load(
                1L,
                2L,
                "Занятие",
                "Кратко",
                "<p>Описание</p>",
                5,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                60,
                7L,
                ActivityType.GROUP,
                null,
                contentStatus,
                moderationStatus,
                moderationComment,
                Set.of(),
                Set.of()
        );
    }

    private ActivitySlot slot(Long id, Long activityId) {
        return ActivitySlot.load(
                id,
                activityId,
                2L,
                2L,
                Instant.parse("2026-05-13T10:00:00Z"),
                Instant.parse("2026-05-13T11:00:00Z"),
                Instant.parse("2026-05-12T10:00:00Z"),
                null,
                ActivitySlotStatus.PLANNED
        );
    }
}
