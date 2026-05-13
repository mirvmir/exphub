package io.github.mirvmir.course.web.response;

import io.github.mirvmir.common.domain.ContentStatus;
import io.github.mirvmir.profile.api.dto.ProfileNameDto;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Set;

public record AuthorCourseResponse(
        Long id,
        Long draftVersionId,
        Long authorId,
        ProfileNameDto author,
        String title,
        String shortDescription,
        String descriptionHtml,
        BigDecimal priceAmount,
        Currency priceCurrency,
        ContentStatus contentStatus,
        Set<Long> topicIds,
        Boolean canEdit,
        Boolean canPublication,
        List<AuthorCourseModuleShortResponse> modules
) {
}