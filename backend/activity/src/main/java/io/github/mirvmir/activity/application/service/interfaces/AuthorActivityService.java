package io.github.mirvmir.activity.application.service.interfaces;

import io.github.mirvmir.activity.web.response.*;
import io.github.mirvmir.activity.web.request.CreateActivityRequest;
import io.github.mirvmir.activity.web.request.CreateGroupActivitySlotRequest;
import io.github.mirvmir.activity.web.request.UpdateActivityRequest;

import java.util.Set;

public interface AuthorActivityService {
    AuthorActivityDescriptionResponse getDescriptionForAuthor(Long activityId);
    Set<IndividualActivitySlotResponse> getIndividualSlotsForAuthor(Long activityId);
    Set<GroupActivitySlotResponse> getGroupSlotsForAuthor(Long activityId);
    IdResponse createActivity(CreateActivityRequest request);
    ActivityResponse updateActivity(Long activityId,
                                    UpdateActivityRequest request);
    void publish(Long activityId);
    void archive(Long activityId);
    void unarchive(Long activityId);
    void deleteActivity(Long activityId);
    ActivitySlotResponse createGroupSlot(Long activityId,
                                         CreateGroupActivitySlotRequest request);
}
