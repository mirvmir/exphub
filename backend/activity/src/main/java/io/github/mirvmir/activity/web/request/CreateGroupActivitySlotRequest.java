package io.github.mirvmir.activity.web.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateGroupActivitySlotRequest(
        @NotNull
        @Future
        Instant startTime
) {
}
