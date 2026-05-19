package io.github.mirvmir.activity.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class ActivityTime {
    private Long id;
    @NonNull
    private Instant startAt;
    @NonNull
    private Instant endAt;
    @NonNull
    private Integer bookingStepMinutes;

    public static ActivityTime create(Instant startAt,
                                      Instant endAt,
                                      Integer bookingStepMinutes) {
        return new ActivityTime(
                null,
                startAt,
                endAt,
                bookingStepMinutes
        );
    }

    public static ActivityTime load(Long id,
                                    Instant startAt,
                                    Instant endAt,
                                    Integer bookingStepMinutes) {
        return new ActivityTime(
                id,
                startAt,
                endAt,
                bookingStepMinutes
        );
    }

    public void assignId(Long id) {
        this.id = id;
    }
}
