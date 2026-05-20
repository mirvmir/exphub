package io.github.mirvmir.course.api.dto;

import java.math.BigDecimal;
import java.util.Currency;

public record CourseBookingInfoResponse(
        Long courseId,
        Long publishedVersionId,
        Long authorId,
        String title,
        BigDecimal priceAmount,
        Currency priceCurrency,
        boolean active
) {
}
