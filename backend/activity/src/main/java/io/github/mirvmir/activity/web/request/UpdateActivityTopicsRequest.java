package io.github.mirvmir.activity.web.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Set;

public record UpdateActivityTopicsRequest(
        @NotNull
        @Positive
        Long subjectId,
        Set<@NotNull @Positive Long> topicIds
) {
}
