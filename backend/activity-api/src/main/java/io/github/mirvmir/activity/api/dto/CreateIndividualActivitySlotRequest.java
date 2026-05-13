package io.github.mirvmir.activity.api.dto;

import java.time.Instant;

public record CreateIndividualActivitySlotRequest(
        Long activityId,
        Long activityTimeId,
        Instant startAt,
        Long studentId
) {
}
