package io.github.mirvmir.activity.application.service.interfaces;

import io.github.mirvmir.activity.web.request.CancelActivitySlotRequest;
import io.github.mirvmir.activity.web.request.UpdateActivitySlotRoomJoinUrlRequest;
import io.github.mirvmir.activity.web.request.UpdateActivityTopicsRequest;
import io.github.mirvmir.activity.web.response.ActivitySlotWithStatusResponse;

import java.util.Set;

public interface ActivitySlotService {
    Set<ActivitySlotWithStatusResponse> getCurrentStudentActivities();
    void cancelByAuthor(Long activitySlotId,
                        CancelActivitySlotRequest request);
    void cancelByStudent(Long activitySlotId,
                         CancelActivitySlotRequest request);
    void complete(Long activitySlotId);
    void updateRoomJoinUrl(Long activitySlotId,
                           UpdateActivitySlotRoomJoinUrlRequest request);
    void updateTopics(Long activitySlotId,
                      UpdateActivityTopicsRequest request);
}