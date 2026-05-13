package io.github.mirvmir.activity.web.response;

import io.github.mirvmir.activity.domain.ActivityType;
import io.github.mirvmir.common.domain.ContentStatus;
import io.github.mirvmir.common.domain.ModerationStatus;
import io.github.mirvmir.profile.api.dto.ProfileNameDto;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Set;

public record AuthorActivityDescriptionResponse(
        Long id,
        Long authorId,
        ProfileNameDto author,
        String title,
        String shortDescription,
        String descriptionHtml,
        Integer maxBookableSeats,
        BigDecimal priceAmount,
        Currency priceCurrency,
        Integer durationMinutes,
        ContentStatus contentStatus,
        ModerationStatus moderationStatus,
        String moderationComment,
        ActivityType type,
        Long subjectId,
        Set<Long> topicIds,

        Set<ActivityTimeResponse> activityTimes,
        Set<IndividualActivitySlotResponse> individualActivities,
        Set<GroupActivitySlotResponse> groupActivities,

        Boolean canEdit,
        Boolean canDelete,
        Boolean canRequestPublication
) {
}