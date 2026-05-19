package io.github.mirvmir.activity.application.service.implementation;

import io.github.mirvmir.activity.application.service.interfaces.IndividualActivityAvailabilityService;
import io.github.mirvmir.activity.domain.Activity;
import io.github.mirvmir.activity.domain.ActivitySlot;
import io.github.mirvmir.activity.domain.ActivityTime;
import io.github.mirvmir.activity.web.response.IndividualActivitySlotResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DefaultIndividualActivityAvailabilityService
        implements IndividualActivityAvailabilityService {

    @Override
    public Set<IndividualActivitySlotResponse> calculateAvailableTimes(Activity activity,
                                                                       List<ActivitySlot> plannedSlots) {
        if (activity.getActivityTimes() == null
                || activity.getActivityTimes().isEmpty()) {
            log.debug("No availability times for individual activity: activityId={}", activity.getId());
            return Set.of();
        }

        Integer durationMinutes = activity.getDurationMinutes();
        if (durationMinutes <= 0) {
            log.error("Invalid activity duration for individual activity availability calculation: activityId={}, durationMinutes={}",
                    activity.getId(),
                    activity.getDurationMinutes());
            throw new IllegalStateException("Длительность занятия должна быть больше 0");
        }

        List<ActivitySlot> safePlannedSlots = plannedSlots == null
                ? List.of()
                : plannedSlots;

        return activity.getActivityTimes()
                .stream()
                .sorted(Comparator.comparing(ActivityTime::getStartAt))
                .flatMap(activityTime -> calculateForActivityTime(
                        activity.getId(),
                        activityTime,
                        safePlannedSlots,
                        durationMinutes
                ).stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<IndividualActivitySlotResponse> calculateForActivityTime(Long activityId,
                                                                          ActivityTime activityTime,
                                                                          List<ActivitySlot> plannedSlots,
                                                                          Integer durationMinutes) {
        Instant windowStart = activityTime.getStartAt();
        Instant windowEnd = activityTime.getEndAt();
        Integer stepMinutes = activityTime.getBookingStepMinutes();
        if (stepMinutes <= 0) {
            log.error("Invalid booking step configuration: stepMinutes must be greater than 0, activityId={}, stepMinutes={}",
                    activityId,
                    stepMinutes);
            throw new IllegalStateException("Шаг бронирования должен быть больше 0");
        }

        List<ActivitySlot> busySlots = plannedSlots.stream()
                .filter(slot -> isOverlapping(
                        windowStart,
                        windowEnd,
                        slot.getStartAt(),
                        slot.getEndAt()
                ))
                .sorted(Comparator.comparing(ActivitySlot::getStartAt))
                .toList();

        List<IndividualActivitySlotResponse> result = new ArrayList<>();

        Instant freeStart = windowStart;

        for (ActivitySlot busySlot : busySlots) {
            Instant busyStart = max(busySlot.getStartAt(), windowStart);
            Instant busyEnd = min(busySlot.getEndAt(), windowEnd);

            addAvailableStarts(
                    result,
                    activityId,
                    freeStart,
                    busyStart,
                    durationMinutes,
                    stepMinutes
            );

            if (busyEnd.isAfter(freeStart)) {
                freeStart = busyEnd;
            }
        }

        addAvailableStarts(
                result,
                activityId,
                freeStart,
                windowEnd,
                durationMinutes,
                stepMinutes
        );

        return result;
    }

    private void addAvailableStarts(List<IndividualActivitySlotResponse> result,
                                    Long activityId,
                                    Instant startTime,
                                    Instant endTime,
                                    Integer durationMinutes,
                                    Integer stepMinutes) {
        Duration duration = Duration.ofMinutes(durationMinutes);
        Duration step = Duration.ofMinutes(stepMinutes);

        Instant slotStart = startTime;

        while (!slotStart.plus(duration).isAfter(endTime)) {
            result.add(new IndividualActivitySlotResponse(
                    null,
                    activityId,
                    slotStart,
                    slotStart.plus(duration)
            ));

            slotStart = slotStart.plus(step);
        }
    }

    private boolean isOverlapping(Instant windowStart,
                                  Instant windowEnd,
                                  Instant slotStart,
                                  Instant slotEnd) {
        return slotStart.isBefore(windowEnd)
                && slotEnd.isAfter(windowStart);
    }

    private Instant max(Instant first, Instant second) {
        return first.isAfter(second) ? first : second;
    }

    private Instant min(Instant first, Instant second) {
        return first.isBefore(second) ? first : second;
    }
}