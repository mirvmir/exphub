package io.github.mirvmir.activity.api.event;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;

public record ActivityPublishedEvent(
        Long activityId,
        Long authorId,
        String title,
        String shortDescription,
        BigDecimal priceAmount,
        Currency priceCurrency,
        Integer durationMinutes,
        Long subjectId,
        String activityType,
        Set<Long> topicIds
) {
}
