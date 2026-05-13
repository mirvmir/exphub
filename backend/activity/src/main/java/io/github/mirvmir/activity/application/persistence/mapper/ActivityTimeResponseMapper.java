package io.github.mirvmir.activity.application.persistence.mapper;

import io.github.mirvmir.activity.web.response.ActivityTimeResponse;
import io.github.mirvmir.activity.domain.ActivityTime;
import org.mapstruct.Mapper;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ActivityTimeResponseMapper {

    ActivityTimeResponse toResponse(ActivityTime activityTime);

    default Set<ActivityTimeResponse> toResponseSet(
            Set<ActivityTime> activityTimes
    ) {
        if (activityTimes == null) {
            return new LinkedHashSet<>();
        }

        return activityTimes.stream()
                .map(this::toResponse)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}