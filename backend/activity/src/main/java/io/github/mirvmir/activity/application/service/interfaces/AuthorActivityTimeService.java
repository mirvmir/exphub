package io.github.mirvmir.activity.application.service.interfaces;

import io.github.mirvmir.activity.web.response.ActivityTimeResponse;
import io.github.mirvmir.activity.web.request.CreateAvailabilityTimeRequest;

import java.util.Set;

public interface AuthorActivityTimeService {
    ActivityTimeResponse createAvailabilityTime(Long activityId,
                                                CreateAvailabilityTimeRequest request);
    Set<ActivityTimeResponse> getAvailabilityTimes(Long activityId);
    void deleteAvailabilityTime(Long activityId, Long activityTimeId);
}
