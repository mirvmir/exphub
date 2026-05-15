package io.github.mirvmir.catalog.application.event;

import io.github.mirvmir.catalog.application.service.interfaces.CatalogService;
import io.github.mirvmir.profile.api.event.ProfileUpdated;
import io.github.mirvmir.review.api.event.ReviewPublishedEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Component
public class CatalogEventListener {

    private final CatalogService catalogService;

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
    public void handle(ProfileUpdated event) {
        catalogService.updateProfile(
                event.userId(),
                event.newGivenName(),
                event.newFamilyName()
        );
    }
}
