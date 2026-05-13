package io.github.mirvmir.activity.web.request;

import java.time.Instant;

public record CreateGroupActivitySlotRequest(
        Instant startTime
) {
}
