package io.github.mirvmir.activity.api.dto;

import java.time.Instant;

public record CreatedActivitySlotResponse(
        Long activitySlotId,
        Long activityId,
        Instant startsAt,
        Instant endsAt
) {
}
