package io.github.mirvmir.activity.web.request;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;

public record UpdateActivityRequest(
        String title,
        String shortDescription,
        String descriptionHtml,
        Integer maxBookableSeats,
        BigDecimal priceAmount,
        Currency priceCurrency,
        Integer durationMinutes,
        Long subjectId,
        Set<Long> topicIds
) {
}
