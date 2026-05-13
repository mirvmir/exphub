package io.github.mirvmir.course.web.response;

import io.github.mirvmir.profile.api.dto.ProfileNameDto;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;

public record CourseInfoResponse(
        Long id,
        Long authorId,
        ProfileNameDto author,
        String title,
        String shortDescription,
        String descriptionHtml,
        BigDecimal priceAmount,
        Currency priceCurrency,
        Set<Long> topicIds,
        Boolean isStudent
) {
}