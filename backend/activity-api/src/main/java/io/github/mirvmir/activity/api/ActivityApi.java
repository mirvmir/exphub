package io.github.mirvmir.activity.api;

import io.github.mirvmir.activity.api.dto.ActivityPurchaseInfoResponse;
import io.github.mirvmir.activity.api.dto.CreateIndividualActivitySlotRequest;
import io.github.mirvmir.activity.api.dto.ActivitySlotPurchaseInfoResponse;
import io.github.mirvmir.activity.api.dto.CreatedActivitySlotResponse;

public interface ActivityApi {
    ActivitySlotPurchaseInfoResponse getSlotPurchaseInfo(Long activitySlotId);
    ActivityPurchaseInfoResponse getPurchaseInfo(Long activityId);
    CreatedActivitySlotResponse createIndividualSlot(CreateIndividualActivitySlotRequest request);
}
