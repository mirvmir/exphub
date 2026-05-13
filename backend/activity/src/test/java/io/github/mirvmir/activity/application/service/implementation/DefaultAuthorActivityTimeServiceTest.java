package io.github.mirvmir.activity.application.service.implementation;

import io.github.mirvmir.activity.application.persistence.mapper.ActivityTimeResponseMapper;
import io.github.mirvmir.activity.application.service.port.repository.ActivityRepository;
import io.github.mirvmir.activity.domain.Activity;
import io.github.mirvmir.activity.domain.ActivityTime;
import io.github.mirvmir.activity.domain.ActivityType;
import io.github.mirvmir.activity.exception.ActivityErrorCode;
import io.github.mirvmir.activity.web.request.CreateAvailabilityTimeRequest;
import io.github.mirvmir.activity.web.response.ActivityTimeResponse;
import io.github.mirvmir.common.domain.ContentStatus;
import io.github.mirvmir.common.domain.ModerationStatus;
import io.github.mirvmir.common.exception.ForbiddenException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.identity.api.IdentityApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultAuthorActivityTimeServiceTest {

    private IdentityApi identityApi;
    private ActivityRepository activityRepository;
    private ActivityTimeResponseMapper activityTimeResponseMapper;
    private DefaultAuthorActivityTimeService service;

    private final Clock clock = Clock.fixed(Instant.parse("2026-05-12T10:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        identityApi = mock(IdentityApi.class);
        activityRepository = mock(ActivityRepository.class);
        activityTimeResponseMapper = mock(ActivityTimeResponseMapper.class);

        service = new DefaultAuthorActivityTimeService(
                identityApi,
                activityRepository,
                activityTimeResponseMapper,
                clock
        );
    }

    @Test
    void createAvailabilityTime_shouldCreateTimeAndReturnResponse() {
        Activity activity = individualActivity();
        Instant startAt = Instant.parse("2026-05-13T10:00:00Z");
        ActivityTimeResponse expected = new ActivityTimeResponse(null, startAt, startAt.plusSeconds(3600));

        when(activityRepository.findById(activity.getId())).thenReturn(activity);
        when(identityApi.getCurrentUserId()).thenReturn(activity.getAuthorId());
        when(activityTimeResponseMapper.toResponse(any(ActivityTime.class))).thenReturn(expected);

        ActivityTimeResponse result = service.createAvailabilityTime(
                activity.getId(),
                new CreateAvailabilityTimeRequest(startAt)
        );

        assertEquals(expected, result);
        assertEquals(1, activity.getActivityTimes().size());
        verify(activityRepository).saveOrUpdate(activity);
        verify(activityTimeResponseMapper).toResponse(any(ActivityTime.class));
    }

    @Test
    void createAvailabilityTime_shouldThrowForbidden_whenCurrentUserIsNotAuthor() {
        Activity activity = individualActivity();

        when(activityRepository.findById(activity.getId())).thenReturn(activity);
        when(identityApi.getCurrentUserId()).thenReturn(99L);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> service.createAvailabilityTime(
                        activity.getId(),
                        new CreateAvailabilityTimeRequest(Instant.parse("2026-05-13T10:00:00Z"))
                ));
        verify(activityRepository, never()).saveOrUpdate(any());
    }

    @Test
    void createAvailabilityTime_shouldThrowNotFound_whenActivityDoesNotExist() {
        when(activityRepository.findById(1L)).thenReturn(null);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> service.createAvailabilityTime(
                        1L,
                        new CreateAvailabilityTimeRequest(Instant.parse("2026-05-13T10:00:00Z"))
                ));
        verifyNoInteractions(identityApi, activityTimeResponseMapper);
    }

    private Activity individualActivity() {
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
                ContentStatus.ACTIVE,
                ModerationStatus.APPROVED,
                null,
                Set.of(11L),
                new HashSet<>()
        );
    }
}
