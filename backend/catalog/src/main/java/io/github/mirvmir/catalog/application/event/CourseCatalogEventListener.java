package io.github.mirvmir.catalog.application.event;


import io.github.mirvmir.catalog.application.service.interfaces.CourseCatalogService;
import io.github.mirvmir.catalog.application.service.interfaces.CatalogService;
import io.github.mirvmir.course.api.event.CourseChangeTopicIds;
import io.github.mirvmir.course.api.event.CourseDeleteEvent;
import io.github.mirvmir.course.api.event.CoursePublishedEvent;
import io.github.mirvmir.review.api.event.ReviewPublishedEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Component
public class CourseCatalogEventListener {

    private final CourseCatalogService courseCatalogService;
    private final CatalogService catalogService;

    @EventListener
    public void handle(CoursePublishedEvent event) {
        courseCatalogService.addCourseToCatalog(event);
    }

    @EventListener
    public void handle(CourseDeleteEvent event) {
        courseCatalogService.removeCourseFromCatalog(event.courseId());
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
    public void handle(CourseChangeTopicIds event) {
        courseCatalogService.updateTopicIds(
                event.courseId(),
                event.topicIds()
        );
    }
}
