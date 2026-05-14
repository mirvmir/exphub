package io.github.mirvmir.activity.application.service.port.repository;

import io.github.mirvmir.activity.domain.ActivitySlot;

import java.util.List;

public interface ActivitySlotRepository {
    ActivitySlot saveOrUpdate(ActivitySlot slot);
    List<ActivitySlot> findPlannedByActivityId(Long activityId);
    List<ActivitySlot> findByActivityId(Long activityId);
    ActivitySlot findById(Long activitySlotId);
    boolean existsPlannedByActivityId(Long activityId);
    ActivitySlot saveGroupSlotWithAuthorLock(Long authorId,
                                             ActivitySlot slot);
    ActivitySlot saveIndividualSlotWithAuthorLock(Long authorId,
                                                  Long activityTimeId,
                                                  ActivitySlot slot);
}