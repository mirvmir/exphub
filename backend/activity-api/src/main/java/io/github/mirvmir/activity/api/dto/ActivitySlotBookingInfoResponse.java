package io.github.mirvmir.activity.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

public record ActivitySlotBookingInfoResponse(
        Long activitySlotId,
        Long activityId,
        Long authorId,
        String title,
        Instant startsAt,
        Instant endsAt,
        Integer capacity,
        BigDecimal priceAmount,
        Currency priceCurrency,
        boolean active
) {
}
