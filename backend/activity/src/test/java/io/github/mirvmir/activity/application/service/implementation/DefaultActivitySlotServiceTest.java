package io.github.mirvmir.activity.application.service.implementation;

import io.github.mirvmir.activity.api.event.ActivityDeleteEvent;
import io.github.mirvmir.activity.application.properties.ActivityCancellationProperties;
import io.github.mirvmir.activity.application.service.port.event.ActivityEventPublisher;
import io.github.mirvmir.activity.application.service.port.repository.ActivityRepository;
import io.github.mirvmir.activity.application.service.port.repository.ActivitySlotRepository;
import io.github.mirvmir.activity.domain.Activity;
import io.github.mirvmir.activity.domain.ActivitySlot;
import io.github.mirvmir.activity.domain.ActivitySlotStatus;
import io.github.mirvmir.activity.domain.ActivityType;
import io.github.mirvmir.activity.exception.ActivityErrorCode;
import io.github.mirvmir.activity.web.request.CancelActivitySlotRequest;
import io.github.mirvmir.activity.web.request.UpdateActivitySlotRoomJoinUrlRequest;
import io.github.mirvmir.common.domain.ContentStatus;
import io.github.mirvmir.common.domain.ModerationStatus;
import io.github.mirvmir.common.exception.ForbiddenException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.payment.api.PaymentApi;
import io.github.mirvmir.taxonomy.api.TaxonomyApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultActivitySlotServiceTest {

    private IdentityApi identityApi;
    private EnrollmentApi enrollmentApi;
    private TaxonomyApi taxonomyApi;
    private ActivityRepository activityRepository;
    private ActivitySlotRepository activitySlotRepository;
    private ActivityCancellationProperties cancellationProperties;
    private ActivityEventPublisher activityEventPublisher;

    private DefaultActivitySlotService service;

    private final Clock clock = Clock.fixed(Instant.parse("2026-05-12T10:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        identityApi = mock(IdentityApi.class);
        enrollmentApi = mock(EnrollmentApi.class);
        taxonomyApi = mock(TaxonomyApi.class);
        activityRepository = mock(ActivityRepository.class);
        activitySlotRepository = mock(ActivitySlotRepository.class);
        cancellationProperties = mock(ActivityCancellationProperties.class);
        activityEventPublisher = mock(ActivityEventPublisher.class);

        service = new DefaultActivitySlotService(
                identityApi,
                enrollmentApi,
                taxonomyApi,
                activityRepository,
                activitySlotRepository,
                cancellationProperties,
                activityEventPublisher,
                clock
        );
    }

    @Test
    void cancelByAuthor_shouldCancelSlotAndCancelEnrollments() {
        Activity activity = groupActivity();
        ActivitySlot slot = slot(10L, activity.getId(), activity.getAuthorId(), activity.getAuthorId());
        CancelActivitySlotRequest request = new CancelActivitySlotRequest("Причина отмены");

        when(identityApi.getCurrentUserId()).thenReturn(activity.getAuthorId());
        when(activitySlotRepository.findById(slot.getId())).thenReturn(slot);
        when(activityRepository.findById(activity.getId())).thenReturn(activity);
        when(cancellationProperties.getMinHoursBeforeStart()).thenReturn(1L);

        service.cancelByAuthor(slot.getId(), request);

        assertEquals(ActivitySlotStatus.CANCELLED, slot.getStatus());
        verify(activitySlotRepository).saveOrUpdate(slot);
        verify(enrollmentApi).cancelAllByActivitySlotId(slot.getId(), request.reason());
        verify(activityEventPublisher).delete(any(ActivityDeleteEvent.class));
    }

    @Test
    void cancelByAuthor_shouldThrowForbidden_whenCurrentUserIsNotAuthor() {
        Activity activity = groupActivity();
        ActivitySlot slot = slot(10L, activity.getId(), activity.getAuthorId(), activity.getAuthorId());

        when(identityApi.getCurrentUserId()).thenReturn(99L);
        when(activitySlotRepository.findById(slot.getId())).thenReturn(slot);
        when(activityRepository.findById(activity.getId())).thenReturn(activity);

        assertThrows(ForbiddenException.class,
                () -> service.cancelByAuthor(slot.getId(), new CancelActivitySlotRequest("Причина")));
        verify(activitySlotRepository, never()).saveOrUpdate(any());
        verifyNoInteractions(enrollmentApi, activityEventPublisher);
    }

    @Test
    void cancelByStudent_shouldCancelIndividualSlotAndEnrollment() {
        Activity activity = individualActivity();
        ActivitySlot slot = slot(10L, activity.getId(), activity.getAuthorId(), 99L);
        CancelActivitySlotRequest request = new CancelActivitySlotRequest("Не смогу прийти");

        when(identityApi.getCurrentUserId()).thenReturn(99L);
        when(activitySlotRepository.findById(slot.getId())).thenReturn(slot);
        when(activityRepository.findById(activity.getId())).thenReturn(activity);
        when(cancellationProperties.getMinHoursBeforeStart()).thenReturn(1L);

        service.cancelByStudent(slot.getId(), request);

        assertEquals(ActivitySlotStatus.CANCELLED, slot.getStatus());
        verify(activitySlotRepository).saveOrUpdate(slot);
        verify(enrollmentApi).cancelByActivitySlotIdAndStudentId(slot.getId(), 99L, request.reason());
        verify(activityEventPublisher).delete(any(ActivityDeleteEvent.class));
    }

    @Test
    void cancelByStudent_shouldCancelOnlyEnrollmentForGroupSlot() {
        Activity activity = groupActivity();
        ActivitySlot slot = slot(10L, activity.getId(), activity.getAuthorId(), activity.getAuthorId());
        CancelActivitySlotRequest request = new CancelActivitySlotRequest("Не смогу прийти");

        when(identityApi.getCurrentUserId()).thenReturn(99L);
        when(activitySlotRepository.findById(slot.getId())).thenReturn(slot);
        when(activityRepository.findById(activity.getId())).thenReturn(activity);

        service.cancelByStudent(slot.getId(), request);

        assertEquals(ActivitySlotStatus.PLANNED, slot.getStatus());
        verify(activitySlotRepository, never()).saveOrUpdate(any());
        verify(enrollmentApi).cancelByActivitySlotIdAndStudentId(slot.getId(), 99L, request.reason());
        verify(activityEventPublisher).delete(any(ActivityDeleteEvent.class));
    }

    @Test
    void complete_shouldCompleteSlot() {
        Activity activity = groupActivity();
        ActivitySlot slot = slot(10L, activity.getId(), activity.getAuthorId(), activity.getAuthorId());

        when(identityApi.getCurrentUserId()).thenReturn(activity.getAuthorId());
        when(activitySlotRepository.findById(slot.getId())).thenReturn(slot);
        when(activityRepository.findById(activity.getId())).thenReturn(activity);

        service.complete(slot.getId());

        assertEquals(ActivitySlotStatus.COMPLETED, slot.getStatus());
        verify(activitySlotRepository).saveOrUpdate(slot);
    }

    @Test
    void updateRoomJoinUrl_shouldUpdateSlotRoomJoinUrl() {
        Activity activity = groupActivity();
        ActivitySlot slot = slot(10L, activity.getId(), activity.getAuthorId(), activity.getAuthorId());
        UpdateActivitySlotRoomJoinUrlRequest request = new UpdateActivitySlotRoomJoinUrlRequest("https://meet.test/room");

        when(identityApi.getCurrentUserId()).thenReturn(activity.getAuthorId());
        when(activitySlotRepository.findById(slot.getId())).thenReturn(slot);
        when(activityRepository.findById(activity.getId())).thenReturn(activity);

        service.updateRoomJoinUrl(slot.getId(), request);

        assertEquals(request.roomJoinUrl(), slot.getRoomJoinUrl());
        verify(activitySlotRepository).saveOrUpdate(slot);
    }

    @Test
    void updateRoomJoinUrl_shouldThrowNotFound_whenSlotDoesNotExist() {
        when(activitySlotRepository.findById(10L)).thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> service.updateRoomJoinUrl(
                        10L,
                        new UpdateActivitySlotRoomJoinUrlRequest("https://meet.test/room")
                )
        );
        verifyNoInteractions(activityRepository);
    }

    private Activity individualActivity() {
        return Activity.load(
                1L,
                2L,
                "Индивидуальное",
                "Кратко",
                "<p>Описание</p>",
                1,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                60,
                7L,
                ActivityType.INDIVIDUAL,
                30,
                ContentStatus.ACTIVE,
                ModerationStatus.APPROVED,
                null,
                Set.of(),
                Set.of()
        );
    }

    private Activity groupActivity() {
        return Activity.load(
                1L,
                2L,
                "Групповое",
                "Кратко",
                "<p>Описание</p>",
                5,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                60,
                7L,
                ActivityType.GROUP,
                null,
                ContentStatus.ACTIVE,
                ModerationStatus.APPROVED,
                null,
                Set.of(), Set.of()
        );
    }

    private ActivitySlot slot(Long id,
                              Long activityId,
                              Long teacherId,
                              Long createdByUserId) {
        return ActivitySlot.load(
                id,
                activityId,
                teacherId,
                createdByUserId,
                Instant.parse("2026-05-13T10:00:00Z"),
                Instant.parse("2026-05-13T11:00:00Z"),
                Instant.parse("2026-05-12T10:00:00Z"),
                null,
                ActivitySlotStatus.PLANNED
        );
    }
}
