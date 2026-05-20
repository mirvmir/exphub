package io.github.mirvmir.activity.api;

import io.github.mirvmir.activity.api.dto.ActivityBookingInfoResponse;
import io.github.mirvmir.activity.api.dto.CreateIndividualActivitySlotRequest;
import io.github.mirvmir.activity.api.dto.ActivitySlotBookingInfoResponse;
import io.github.mirvmir.activity.api.dto.CreatedActivitySlotResponse;

public interface ActivityApi {
    ActivitySlotBookingInfoResponse getSlotBookingInfo(Long activitySlotId);
    ActivityBookingInfoResponse getBookingInfo(Long activityId);
    CreatedActivitySlotResponse createIndividualSlot(CreateIndividualActivitySlotRequest request);
}
