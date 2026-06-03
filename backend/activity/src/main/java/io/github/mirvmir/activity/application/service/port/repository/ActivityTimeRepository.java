package io.github.mirvmir.activity.application.service.port.repository;

import io.github.mirvmir.activity.domain.ActivityTime;

public interface ActivityTimeRepository {
    ActivityTime save(Long activityId,
                      ActivityTime activityTime);
    void deleteByActivityIdAndId(Long activityId,
                                 Long activityTimeId);
}