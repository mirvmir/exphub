package io.github.mirvmir.activity.application.service.port.repository;

public interface ActivityTimeRepository {
    void deleteByActivityIdAndId(Long activityId,
                                 Long activityTimeId);
}