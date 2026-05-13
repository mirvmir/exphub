package io.github.mirvmir.course.api.event;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;

public record CoursePublishedEvent(
        Long courseId,
        Long authorId,
        String title,
        String shortDescription,
        BigDecimal priceAmount,
        Currency priceCurrency,
        Set<Long> topicIds
) {
}
