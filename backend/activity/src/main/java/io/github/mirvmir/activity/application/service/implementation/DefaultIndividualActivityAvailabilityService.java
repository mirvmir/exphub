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

        if (durationMinutes == null || durationMinutes <= 0) {
            log.error("Invalid individual activity duration: activityId={}, durationMinutes={}",
                    activity.getId(),
                    durationMinutes);
            throw new IllegalStateException("Длительность занятия должна быть больше 0");
        }

        Set<IndividualActivitySlotResponse> result = activity.getActivityTimes()
                .stream()
                .flatMap(activityTime -> calculateForActivityTime(
                        activity.getId(),
                        activityTime,
                        plannedSlots == null ? List.of() : plannedSlots,
                        durationMinutes
                ).stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        log.debug("Calculated available individual activity times: activityId={}, availableTimesCount={}",
                activity.getId(),
                result.size());
        return result;
    }

    private List<IndividualActivitySlotResponse> calculateForActivityTime(Long activityId,
                                                                          ActivityTime activityTime,
                                                                          List<ActivitySlot> plannedSlots,
                                                                          Integer durationMinutes) {
        Instant windowStart = activityTime.getStartAt();
        Instant windowEnd = activityTime.getEndAt();

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

            addIfFits(
                    result,
                    activityId,
                    freeStart,
                    busyStart,
                    durationMinutes
            );

            if (busyEnd.isAfter(freeStart)) {
                freeStart = busyEnd;
            }
        }

        addIfFits(
                result,
                activityId,
                freeStart,
                windowEnd,
                durationMinutes
        );

        return result;
    }

    private void addIfFits(List<IndividualActivitySlotResponse> result,
                           Long activityId,
                           Instant startTime,
                           Instant endTime,
                           Integer durationMinutes) {
        long freeMinutes = Duration.between(startTime, endTime).toMinutes();

        if (freeMinutes >= durationMinutes) {
            result.add(new IndividualActivitySlotResponse(
                    null,
                    activityId,
                    startTime,
                    endTime
            ));
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