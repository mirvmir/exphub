package io.github.mirvmir.activity.web.response;

import io.github.mirvmir.activity.domain.ActivitySlotStatus;

import java.time.Instant;

public record ActivityPlannedSlotResponse(
        Long slotId,
        Instant startAt,
        Instant endAt,
        ActivitySlotStatus status,
        Long studentId
) {
}