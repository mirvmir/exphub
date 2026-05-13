package io.github.mirvmir.activity.application.service.interfaces;

import io.github.mirvmir.activity.web.response.ActivityResponse;
import io.github.mirvmir.activity.web.response.ActivitySlotResponse;
import io.github.mirvmir.activity.web.response.AuthorActivityDescriptionResponse;
import io.github.mirvmir.activity.web.response.IdResponse;
import io.github.mirvmir.activity.web.request.CreateActivityRequest;
import io.github.mirvmir.activity.web.request.CreateGroupActivitySlotRequest;
import io.github.mirvmir.activity.web.request.UpdateActivityRequest;

public interface AuthorActivityService {
    AuthorActivityDescriptionResponse getActivityByAuthor(Long activityId);
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
