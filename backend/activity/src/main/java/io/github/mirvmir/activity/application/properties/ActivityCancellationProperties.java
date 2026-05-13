package io.github.mirvmir.activity.application.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ActivityCancellationProperties {

    @Value("${activity.cancellation.min_hours_before_start}")
    private long minHoursBeforeStart;

    public long getMinHoursBeforeStart() {
        return minHoursBeforeStart;
    }
}