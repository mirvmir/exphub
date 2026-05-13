package io.github.mirvmir.activity.web.response;

import io.github.mirvmir.activity.domain.ActivityType;
import io.github.mirvmir.profile.api.dto.ProfileNameDto;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;

public record ActivityDescriptionResponse(
        Long id,
        Long authorId,
        ProfileNameDto author,
        String title,
        String shortDescription,
        String descriptionHtml,
        BigDecimal priceAmount,
        Currency priceCurrency,
        Integer durationMinutes,
        ActivityType type,
        Long subjectId,
        Set<Long> topicIds,
        Boolean isStudent,
        Set<IndividualActivitySlotResponse> availableTimes,
        Set<GroupActivitySlotResponse> availableSlots
) {
}