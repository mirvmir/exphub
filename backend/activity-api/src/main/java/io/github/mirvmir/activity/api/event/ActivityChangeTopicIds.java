package io.github.mirvmir.activity.api.event;

import java.util.Set;

public record ActivityChangeTopicIds(
        Long activityId,
        Set<Long> topicIds
) {
}
