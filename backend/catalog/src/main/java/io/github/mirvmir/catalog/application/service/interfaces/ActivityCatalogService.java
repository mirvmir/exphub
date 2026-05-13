package io.github.mirvmir.catalog.application.service.interfaces;

import io.github.mirvmir.activity.api.event.ActivityPublishedEvent;

import java.util.Set;

public interface ActivityCatalogService {
    void addActivityToCatalog(ActivityPublishedEvent event);
    void removeActivityFromCatalog(Long activityId);
    void updateTopicIds(Long activityId,
                        Set<Long> topicIds);
}
