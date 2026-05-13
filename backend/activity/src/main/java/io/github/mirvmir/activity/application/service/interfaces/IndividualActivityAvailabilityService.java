package io.github.mirvmir.activity.application.service.interfaces;

import io.github.mirvmir.activity.domain.Activity;
import io.github.mirvmir.activity.domain.ActivitySlot;
import io.github.mirvmir.activity.web.response.IndividualActivitySlotResponse;

import java.util.List;
import java.util.Set;

public interface IndividualActivityAvailabilityService {
    Set<IndividualActivitySlotResponse> calculateAvailableTimes(Activity activity,
                                                                List<ActivitySlot> plannedSlots);
}