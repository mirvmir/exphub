package io.github.mirvmir.activity.application.service.implementation;

import io.github.mirvmir.activity.api.event.ActivityDeleteEvent;
import io.github.mirvmir.activity.application.persistence.mapper.ActivityEventMapper;
import io.github.mirvmir.activity.application.persistence.mapper.ActivityResponseMapper;
import io.github.mirvmir.activity.application.persistence.mapper.ActivitySlotResponseMapper;
import io.github.mirvmir.activity.application.persistence.mapper.ActivityTimeResponseMapper;
import io.github.mirvmir.activity.application.service.port.event.ActivityEventPublisher;
import io.github.mirvmir.activity.application.service.port.repository.ActivityRepository;
import io.github.mirvmir.activity.application.service.port.repository.ActivitySlotRepository;
import io.github.mirvmir.activity.domain.Activity;
import io.github.mirvmir.activity.domain.ActivitySlot;
import io.github.mirvmir.activity.domain.ActivitySlotStatus;
import io.github.mirvmir.activity.domain.ActivityTime;
import io.github.mirvmir.activity.domain.ActivityType;
import io.github.mirvmir.activity.exception.ActivityErrorCode;
import io.github.mirvmir.activity.web.request.CreateActivityRequest;
import io.github.mirvmir.activity.web.request.CreateGroupActivitySlotRequest;
import io.github.mirvmir.activity.web.request.UpdateActivityRequest;
import io.github.mirvmir.activity.web.response.ActivityResponse;
import io.github.mirvmir.activity.web.response.ActivitySlotResponse;
import io.github.mirvmir.activity.web.response.AuthorActivityDescriptionResponse;
import io.github.mirvmir.activity.web.response.GroupActivitySlotResponse;
import io.github.mirvmir.common.domain.ContentStatus;
import io.github.mirvmir.common.domain.ModerationStatus;
import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.ForbiddenException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.profile.api.ProfileApi;
import io.github.mirvmir.profile.api.dto.ProfileNameDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultAuthorActivityServiceTest {

    private IdentityApi identityApi;
    private ProfileApi profileApi;
    private EnrollmentApi enrollmentApi;
    private ActivityRepository activityRepository;
    private ActivitySlotRepository activitySlotRepository;
    private ActivityResponseMapper activityResponseMapper;
    private ActivitySlotResponseMapper activitySlotResponseMapper;
    private ActivityEventMapper activityEventMapper;
    private ActivityEventPublisher activityEventPublisher;

    private DefaultAuthorActivityService service;

    private final Clock clock = Clock.fixed(Instant.parse("2026-05-12T10:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        identityApi = mock(IdentityApi.class);
        profileApi = mock(ProfileApi.class);
        enrollmentApi = mock(EnrollmentApi.class);
        activityRepository = mock(ActivityRepository.class);
        activitySlotRepository = mock(ActivitySlotRepository.class);
        activityResponseMapper = mock(ActivityResponseMapper.class);
        activitySlotResponseMapper = mock(ActivitySlotResponseMapper.class);
        activityEventMapper = mock(ActivityEventMapper.class);
        activityEventPublisher = mock(ActivityEventPublisher.class);

        service = new DefaultAuthorActivityService(
                identityApi,
                profileApi,
                enrollmentApi,
                activityRepository,
                activitySlotRepository,
                activityResponseMapper,
                activitySlotResponseMapper,
                activityEventMapper,
                activityEventPublisher,
                clock
        );
    }

    @Test
    void getDescriptionForAuthor_shouldReturnActivityDescription() {
        Activity activity = groupActivity(ContentStatus.ACTIVE, ModerationStatus.APPROVED);
        ProfileNameDto author = mock(ProfileNameDto.class);
        AuthorActivityDescriptionResponse expected = mock(AuthorActivityDescriptionResponse.class);

        when(activityRepository.findById(activity.getId())).thenReturn(activity);
        when(identityApi.getCurrentUserId()).thenReturn(activity.getAuthorId());
        when(profileApi.getProfileName(activity.getAuthorId())).thenReturn(author);
        when(activitySlotRepository.existsPlannedByActivityId(activity.getId())).thenReturn(true);
        when(activityResponseMapper.toAuthorActivityDescriptionResponse(
                eq(activity), eq(author), eq(false), eq(false), eq(false)
        )).thenReturn(expected);

        AuthorActivityDescriptionResponse result = service.getDescription(activity.getId());

        assertSame(expected, result);
    }

    @Test
    void getDescriptionForAuthor_shouldThrowForbidden_whenCurrentUserIsNotAuthor() {
        Activity activity = groupActivity(ContentStatus.ACTIVE, ModerationStatus.APPROVED);

        when(activityRepository.findById(activity.getId())).thenReturn(activity);
        when(identityApi.getCurrentUserId()).thenReturn(99L);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> service.getDescription(activity.getId()));
        verifyNoInteractions(profileApi, enrollmentApi);
    }

    @Test
    void createActivity_shouldCreateGroupActivity() {
        CreateActivityRequest request = new CreateActivityRequest(
                "Групповое занятие",
                "Кратко",
                "<p>Описание</p>",
                5,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                60,
                ActivityType.GROUP,
                null
        );

        when(identityApi.getCurrentUserId()).thenReturn(2L);
        when(activityRepository.saveOrUpdate(any(Activity.class))).thenAnswer(invocation -> {
            Activity activity = invocation.getArgument(0);
            activity.assignId(1L);
            return activity;
        });

        var result = service.createActivity(request);

        assertEquals(1L, result.id());
        ArgumentCaptor<Activity> activityCaptor = ArgumentCaptor.forClass(Activity.class);
        verify(activityRepository).saveOrUpdate(activityCaptor.capture());
        assertEquals(ActivityType.GROUP, activityCaptor.getValue().getType());
        assertEquals(2L, activityCaptor.getValue().getAuthorId());
    }

    @Test
    void updateActivity_shouldUpdateAndReturnResponse() {
        Activity activity = groupActivity(ContentStatus.DRAFT, ModerationStatus.DRAFT);
        UpdateActivityRequest request = new UpdateActivityRequest(
                "Новое название",
                "Новое краткое",
                "<p>Новое описание</p>",
                4,
                new BigDecimal("2000"),
                Currency.getInstance("RUB"),
                90
        );
        ActivityResponse expected = mock(ActivityResponse.class);

        when(activityRepository.findById(activity.getId())).thenReturn(activity);
        when(identityApi.getCurrentUserId()).thenReturn(activity.getAuthorId());
        when(activityResponseMapper.toActivityDto(activity)).thenReturn(expected);

        ActivityResponse result = service.updateActivity(activity.getId(), request);

        assertSame(expected, result);
        assertEquals("Новое название", activity.getTitle());
        assertEquals(4, activity.getMaxBookableSeats());
        verify(activityRepository).saveOrUpdate(activity);
    }

    @Test
    void publish_shouldRequestPublication() {
        Activity activity = groupActivity(ContentStatus.DRAFT, ModerationStatus.DRAFT);

        when(activityRepository.findById(activity.getId())).thenReturn(activity);
        when(identityApi.getCurrentUserId()).thenReturn(activity.getAuthorId());

        service.publish(activity.getId());

        assertEquals(ModerationStatus.PENDING, activity.getModerationStatus());
        verify(activityRepository).saveOrUpdate(activity);
    }

    @Test
    void archive_shouldArchiveActivityAndDeletePublishedEvent() {
        Activity activity = groupActivity(ContentStatus.ACTIVE, ModerationStatus.APPROVED);

        when(activityRepository.findById(activity.getId())).thenReturn(activity);
        when(identityApi.getCurrentUserId()).thenReturn(activity.getAuthorId());

        service.archive(activity.getId());

        assertEquals(ContentStatus.ARCHIVED, activity.getContentStatus());
        verify(activityEventPublisher).delete(any(ActivityDeleteEvent.class));
        verify(activityRepository).saveOrUpdate(activity);
    }

    @Test
    void deleteActivity_shouldThrowBusinessException_whenActivityHasPlannedSlots() {
        Activity activity = groupActivity(ContentStatus.ACTIVE, ModerationStatus.APPROVED);

        when(activityRepository.findById(activity.getId())).thenReturn(activity);
        when(identityApi.getCurrentUserId()).thenReturn(activity.getAuthorId());
        when(activitySlotRepository.existsPlannedByActivityId(activity.getId())).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.deleteActivity(activity.getId()));
        verify(activityRepository, never()).saveOrUpdate(any());
    }

    @Test
    void createGroupSlot_shouldCreateSlotWithAuthorLock() {
        Activity activity = groupActivity(ContentStatus.ACTIVE, ModerationStatus.APPROVED);
        Instant startAt = Instant.parse("2026-05-13T10:00:00Z");
        ActivitySlot savedSlot = slot(10L, activity.getId());

        when(activityRepository.findById(activity.getId())).thenReturn(activity);
        when(identityApi.getCurrentUserId()).thenReturn(activity.getAuthorId());
        when(activitySlotRepository.saveGroupSlotWithAuthorLock(eq(activity.getAuthorId()), any(ActivitySlot.class)))
                .thenReturn(savedSlot);

        ActivitySlotResponse result = service.createGroupSlot(
                activity.getId(),
                new CreateGroupActivitySlotRequest(startAt)
        );

        assertEquals(savedSlot.getId(), result.id());
        assertEquals(savedSlot.getActivityId(), result.activityId());
        verify(activitySlotRepository).saveGroupSlotWithAuthorLock(eq(activity.getAuthorId()), any(ActivitySlot.class));
    }

    @Test
    void createGroupSlot_shouldThrowNotFound_whenActivityIsNotActive() {
        Activity activity = groupActivity(ContentStatus.ARCHIVED, ModerationStatus.APPROVED);

        when(activityRepository.findById(activity.getId())).thenReturn(activity);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.createGroupSlot(activity.getId(),
                        new CreateGroupActivitySlotRequest(Instant.parse("2026-05-13T10:00:00Z"))));
    }

    private Activity groupActivity(ContentStatus contentStatus, ModerationStatus moderationStatus) {
        return Activity.load(
                1L,
                2L,
                "Групповое занятие",
                "Кратко",
                "<p>Описание</p>",
                5,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                60,
                7L,
                ActivityType.GROUP,
                contentStatus,
                moderationStatus,
                null,
                Set.of(11L, 12L),
                Set.of(ActivityTime.load(
                                100L,
                                Instant.parse("2026-05-13T10:00:00Z"),
                                Instant.parse("2026-05-13T11:00:00Z"),
                                60
                        )
                )
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
