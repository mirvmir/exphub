package io.github.mirvmir.activity.web.response;

import java.time.Instant;

public record ActivityTimeResponse(
        Long id,
        Instant startAt,
        Instant endAt
) {
}