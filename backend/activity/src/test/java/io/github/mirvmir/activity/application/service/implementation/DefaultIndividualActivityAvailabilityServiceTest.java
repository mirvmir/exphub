package io.github.mirvmir.activity.application.service.implementation;

import io.github.mirvmir.activity.domain.Activity;
import io.github.mirvmir.activity.domain.ActivitySlot;
import io.github.mirvmir.activity.domain.ActivitySlotStatus;
import io.github.mirvmir.activity.domain.ActivityTime;
import io.github.mirvmir.activity.domain.ActivityType;
import io.github.mirvmir.activity.web.response.IndividualActivitySlotResponse;
import io.github.mirvmir.common.domain.ContentStatus;
import io.github.mirvmir.common.domain.ModerationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DefaultIndividualActivityAvailabilityServiceTest {

    private DefaultIndividualActivityAvailabilityService service;

    @BeforeEach
    void setUp() {
        service = new DefaultIndividualActivityAvailabilityService();
    }

    @Test
    void calculateAvailableTimes_shouldReturnWholeWindow_whenNoPlannedSlots() {
        Activity activity = activity(60, Set.of(
                ActivityTime.load(1L,
                        Instant.parse("2026-05-13T10:00:00Z"),
                        Instant.parse("2026-05-13T13:00:00Z"))
        ));

        Set<IndividualActivitySlotResponse> result = service.calculateAvailableTimes(activity, List.of());

        assertEquals(1, result.size());
        IndividualActivitySlotResponse response = result.iterator().next();
        assertEquals(activity.getId(), response.activityId());
        assertEquals(Instant.parse("2026-05-13T10:00:00Z"), response.startTime());
        assertEquals(Instant.parse("2026-05-13T13:00:00Z"), response.endTime());
    }

    @Test
    void calculateAvailableTimes_shouldSplitWindowByBusySlots() {
        Activity activity = activity(60, Set.of(
                ActivityTime.load(1L,
                        Instant.parse("2026-05-13T10:00:00Z"),
                        Instant.parse("2026-05-13T15:00:00Z"))
        ));
        List<ActivitySlot> plannedSlots = List.of(
                slot(10L, activity.getId(),
                        Instant.parse("2026-05-13T11:00:00Z"),
                        Instant.parse("2026-05-13T12:00:00Z")),
                slot(11L, activity.getId(),
                        Instant.parse("2026-05-13T13:00:00Z"),
                        Instant.parse("2026-05-13T14:00:00Z"))
        );

        Set<IndividualActivitySlotResponse> result = service.calculateAvailableTimes(activity, plannedSlots);

        assertEquals(3, result.size());
        assertTrue(result.contains(new IndividualActivitySlotResponse(
                null, activity.getId(),
                Instant.parse("2026-05-13T10:00:00Z"),
                Instant.parse("2026-05-13T11:00:00Z"))));
        assertTrue(result.contains(new IndividualActivitySlotResponse(
                null, activity.getId(),
                Instant.parse("2026-05-13T12:00:00Z"),
                Instant.parse("2026-05-13T13:00:00Z"))));
        assertTrue(result.contains(new IndividualActivitySlotResponse(
                null, activity.getId(),
                Instant.parse("2026-05-13T14:00:00Z"),
                Instant.parse("2026-05-13T15:00:00Z"))));
    }

    @Test
    void calculateAvailableTimes_shouldIgnoreFreeIntervalsLessThanDuration() {
        Activity activity = activity(90, Set.of(
                ActivityTime.load(1L,
                        Instant.parse("2026-05-13T10:00:00Z"),
                        Instant.parse("2026-05-13T13:00:00Z"))
        ));
        List<ActivitySlot> plannedSlots = List.of(
                slot(10L, activity.getId(),
                        Instant.parse("2026-05-13T11:00:00Z"),
                        Instant.parse("2026-05-13T12:00:00Z"))
        );

        Set<IndividualActivitySlotResponse> result = service.calculateAvailableTimes(activity, plannedSlots);

        assertTrue(result.isEmpty());
    }

    @Test
    void calculateAvailableTimes_shouldReturnEmptySet_whenActivityHasNoTimes() {
        Activity activity = activity(60, Set.of());

        Set<IndividualActivitySlotResponse> result = service.calculateAvailableTimes(activity, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void calculateAvailableTimes_shouldThrowIllegalStateException_whenDurationIsInvalid() {
        Activity activity = activity(0, Set.of(
                ActivityTime.load(1L,
                        Instant.parse("2026-05-13T10:00:00Z"),
                        Instant.parse("2026-05-13T13:00:00Z"))
        ));

        assertThrows(IllegalStateException.class,
                () -> service.calculateAvailableTimes(activity, List.of()));
    }

    private Activity activity(Integer durationMinutes, Set<ActivityTime> activityTimes) {
        return Activity.load(
                1L,
                2L,
                "Индивидуальное занятие",
                "Кратко",
                "<p>Описание</p>",
                1,
                new BigDecimal("1500"),
                Currency.getInstance("RUB"),
                durationMinutes,
                7L,
                ActivityType.INDIVIDUAL,
                30,
                ContentStatus.ACTIVE,
                ModerationStatus.APPROVED,
                null,
                Set.of(),
                activityTimes
        );
    }

    private ActivitySlot slot(Long id, Long activityId, Instant startAt, Instant endAt) {
        return ActivitySlot.load(
                id,
                activityId,
                2L,
                2L,
                startAt,
                endAt,
                Instant.parse("2026-05-12T10:00:00Z"),
                null,
                ActivitySlotStatus.PLANNED
        );
    }
}
