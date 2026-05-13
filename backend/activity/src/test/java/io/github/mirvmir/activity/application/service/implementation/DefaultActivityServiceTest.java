package io.github.mirvmir.activity.application.service.implementation;

import io.github.mirvmir.activity.application.persistence.mapper.ActivityResponseMapper;
import io.github.mirvmir.activity.application.persistence.mapper.ActivitySlotResponseMapper;
import io.github.mirvmir.activity.application.service.interfaces.IndividualActivityAvailabilityService;
import io.github.mirvmir.activity.application.service.port.repository.ActivityRepository;
import io.github.mirvmir.activity.application.service.port.repository.ActivitySlotRepository;
import io.github.mirvmir.activity.domain.Activity;
import io.github.mirvmir.activity.domain.ActivitySlot;
import io.github.mirvmir.activity.domain.ActivitySlotStatus;
import io.github.mirvmir.activity.domain.ActivityType;
import io.github.mirvmir.activity.web.response.ActivityDescriptionResponse;
import io.github.mirvmir.activity.web.response.GroupActivitySlotResponse;
import io.github.mirvmir.activity.web.response.IndividualActivitySlotResponse;
import io.github.mirvmir.common.domain.ContentStatus;
import io.github.mirvmir.common.domain.ModerationStatus;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.profile.api.ProfileApi;
import io.github.mirvmir.profile.api.dto.ProfileNameDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

class DefaultActivityServiceTest {

    private IdentityApi identityApi;
    private EnrollmentApi enrollmentApi;
    private ProfileApi profileApi;
    private ActivityRepository activityRepository;
    private ActivitySlotRepository activitySlotRepository;
    private IndividualActivityAvailabilityService individualActivityAvailabilityService;
    private ActivityResponseMapper activityResponseMapper;
    private ActivitySlotResponseMapper activitySlotResponseMapper;

    private DefaultActivityService service;

    private final Clock clock = Clock.fixed(Instant.parse("2026-05-12T10:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        identityApi = mock(IdentityApi.class);
        enrollmentApi = mock(EnrollmentApi.class);
        profileApi = mock(ProfileApi.class);
        activityRepository = mock(ActivityRepository.class);
        activitySlotRepository = mock(ActivitySlotRepository.class);
        individualActivityAvailabilityService = mock(IndividualActivityAvailabilityService.class);
        activityResponseMapper = mock(ActivityResponseMapper.class);
        activitySlotResponseMapper = mock(ActivitySlotResponseMapper.class);

        service = new DefaultActivityService(
                identityApi,
                enrollmentApi,
                profileApi,
                activityRepository,
                activitySlotRepository,
                individualActivityAvailabilityService,
                activityResponseMapper,
                activitySlotResponseMapper,
                clock
        );
    }

    @Test
    void getActivity_shouldReturnActiveIndividualActivityWithAvailableTimes() {
        Activity activity = individualActivity(ContentStatus.ACTIVE, ModerationStatus.APPROVED);
        ProfileNameDto author = mock(ProfileNameDto.class);
        ActivitySlot plannedSlot = slot(10L, activity.getId());
        Set<IndividualActivitySlotResponse> availableTimes = Set.of(
                new IndividualActivitySlotResponse(100L, activity.getId(),
                        Instant.parse("2026-05-13T10:00:00Z"),
                        Instant.parse("2026-05-13T11:00:00Z"))
        );
        ActivityDescriptionResponse expected = mock(ActivityDescriptionResponse.class);

        when(activityRepository.findById(activity.getId())).thenReturn(activity);
        when(identityApi.getCurrentUserId()).thenReturn(99L);
        when(enrollmentApi.isStudentOfActivity(99L, activity.getId())).thenReturn(false);
        when(profileApi.getProfileName(activity.getAuthorId())).thenReturn(author);
        when(activitySlotRepository.findPlannedByActivityId(activity.getId())).thenReturn(List.of(plannedSlot));
        when(individualActivityAvailabilityService.calculateAvailableTimes(activity, List.of(plannedSlot)))
                .thenReturn(availableTimes);
        when(activityResponseMapper.toActivityDescriptionResponse(
                eq(activity), eq(author), eq(false), eq(availableTimes), eq(Set.of())
        )).thenReturn(expected);

        ActivityDescriptionResponse result = service.getActivity(activity.getId());

        assertSame(expected, result);
        verify(activitySlotRepository).findPlannedByActivityId(activity.getId());
        verify(individualActivityAvailabilityService).calculateAvailableTimes(activity, List.of(plannedSlot));
        verify(enrollmentApi, never()).countBookedByActivitySlotIds(anySet(), any());
    }

    @Test
    void getActivity_shouldReturnActiveGroupActivityWithAvailableSlots() {
        Activity activity = groupActivity(ContentStatus.ACTIVE, ModerationStatus.APPROVED);
        ProfileNameDto author = mock(ProfileNameDto.class);
        ActivitySlot plannedSlot = slot(10L, activity.getId());
        Set<GroupActivitySlotResponse> availableSlots = Set.of(
                new GroupActivitySlotResponse(10L, activity.getId(), plannedSlot.getStartAt(), plannedSlot.getEndAt(), 1, 5)
        );
        ActivityDescriptionResponse expected = mock(ActivityDescriptionResponse.class);

        when(activityRepository.findById(activity.getId())).thenReturn(activity);
        when(identityApi.getCurrentUserId()).thenReturn(99L);
        when(enrollmentApi.isStudentOfActivity(99L, activity.getId())).thenReturn(false);
        when(profileApi.getProfileName(activity.getAuthorId())).thenReturn(author);
        when(activitySlotRepository.findPlannedByActivityId(activity.getId())).thenReturn(List.of(plannedSlot));
        when(enrollmentApi.countBookedByActivitySlotIds(Set.of(10L), Instant.now(clock)))
                .thenReturn(Map.of(10L, 1));
        when(activitySlotResponseMapper.toAvailableGroupSlotResponseSet(List.of(plannedSlot), Map.of(10L, 1), 5))
                .thenReturn(availableSlots);
        when(activityResponseMapper.toActivityDescriptionResponse(
                eq(activity), eq(author), eq(false), eq(Set.of()), eq(availableSlots)
        )).thenReturn(expected);

        ActivityDescriptionResponse result = service.getActivity(activity.getId());

        assertSame(expected, result);
        verify(enrollmentApi).countBookedByActivitySlotIds(Set.of(10L), Instant.now(clock));
        verify(individualActivityAvailabilityService, never()).calculateAvailableTimes(any(), anyList());
    }

    @Test
    void getActivity_shouldReturnArchivedActivityOnlyForStudent() {
        Activity activity = individualActivity(ContentStatus.ARCHIVED, ModerationStatus.APPROVED);
        ProfileNameDto author = mock(ProfileNameDto.class);
        ActivityDescriptionResponse expected = mock(ActivityDescriptionResponse.class);

        when(activityRepository.findById(activity.getId())).thenReturn(activity);
        when(identityApi.getCurrentUserId()).thenReturn(99L);
        when(enrollmentApi.isStudentOfActivity(99L, activity.getId())).thenReturn(true);
        when(profileApi.getProfileName(activity.getAuthorId())).thenReturn(author);
        when(activityResponseMapper.toActivityDescriptionResponse(
                eq(activity), eq(author), eq(true), eq(Set.of()), eq(Set.of())
        )).thenReturn(expected);

        ActivityDescriptionResponse result = service.getActivity(activity.getId());

        assertSame(expected, result);
        verify(activitySlotRepository, never()).findPlannedByActivityId(anyLong());
    }

    @Test
    void getActivity_shouldThrowNotFound_whenActivityDoesNotExist() {
        when(activityRepository.findById(1L)).thenReturn(null);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.getActivity(1L));
        verifyNoInteractions(profileApi, activitySlotRepository);
    }

    @Test
    void getActivity_shouldThrowNotFound_whenArchivedActivityRequestedByNotStudent() {
        Activity activity = individualActivity(ContentStatus.ARCHIVED, ModerationStatus.APPROVED);

        when(activityRepository.findById(activity.getId())).thenReturn(activity);
        when(identityApi.getCurrentUserId()).thenReturn(99L);
        when(enrollmentApi.isStudentOfActivity(99L, activity.getId())).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.getActivity(activity.getId()));
        verifyNoInteractions(profileApi, activitySlotRepository);
    }

    private Activity individualActivity(ContentStatus contentStatus, ModerationStatus moderationStatus) {
        return Activity.load(
                1L,
                2L,
                "Индивидуальное занятие",
                "Кратко",
                "<p>Описание</p>",
                1,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                60,
                7L,
                ActivityType.INDIVIDUAL,
                30,
                contentStatus,
                moderationStatus,
                null,
                Set.of(11L, 12L),
                Set.of()
        );
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
                null,
                contentStatus,
                moderationStatus,
                null,
                Set.of(11L, 12L),
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
