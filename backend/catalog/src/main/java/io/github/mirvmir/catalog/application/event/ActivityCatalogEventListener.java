package io.github.mirvmir.catalog.application.event;

import io.github.mirvmir.activity.api.event.ActivityChangeTopicIds;
import io.github.mirvmir.activity.api.event.ActivityDeleteEvent;
import io.github.mirvmir.activity.api.event.ActivityPublishedEvent;
import io.github.mirvmir.catalog.application.service.interfaces.ActivityCatalogService;
import io.github.mirvmir.catalog.application.service.interfaces.CatalogService;
import io.github.mirvmir.review.api.event.ReviewPublishedEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Component
public class ActivityCatalogEventListener {

    private final ActivityCatalogService activityCatalogService;
    private final CatalogService catalogService;

    @EventListener
    public void handle(ActivityPublishedEvent event) {
        activityCatalogService.addActivityToCatalog(event);
    }

    @EventListener
    public void handle(ActivityDeleteEvent event) {
        activityCatalogService.removeActivityFromCatalog(event.activityId());
    }

    @EventListener
    public void handle(ReviewPublishedEvent event) {
        catalogService.addScore(
                event.activityId(),
                event.courseId(),
                event.score()
        );
    }

    @EventListener
    @Transactional
    public void handle(ActivityChangeTopicIds event) {
        activityCatalogService.updateTopicIds(
                event.activityId(),
                event.topicIds()
        );
    }
}