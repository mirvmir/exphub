package io.github.mirvmir.activity.application.service.interfaces;

import io.github.mirvmir.activity.web.response.ActivityTimeResponse;
import io.github.mirvmir.activity.web.request.CreateAvailabilityTimeRequest;

public interface AuthorActivityTimeService {
    ActivityTimeResponse createAvailabilityTime(Long activityId,
                                                CreateAvailabilityTimeRequest request);
}
