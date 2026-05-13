package io.github.mirvmir.activity.application.persistence.mapper;

import io.github.mirvmir.activity.api.event.ActivityPublishedEvent;
import io.github.mirvmir.activity.domain.Activity;
import org.springframework.stereotype.Component;

@Component
public class ActivityEventMapper {

    public ActivityPublishedEvent toPublishedEvent(Activity activity) {
        return new ActivityPublishedEvent(
                activity.getId(),
                activity.getAuthorId(),
                activity.getTitle(),
                activity.getShortDescription(),
                activity.getPrice().getAmount(),
                activity.getPrice().getCurrency(),
                activity.getDurationMinutes(),
                activity.getSubjectId(),
                activity.getType().name(),
                activity.getTopicIds()
        );
    }
}