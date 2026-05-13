package io.github.mirvmir.activity.web.controller;

import io.github.mirvmir.activity.application.service.interfaces.ActivitySlotService;
import io.github.mirvmir.activity.application.service.interfaces.AuthorActivityService;
import io.github.mirvmir.activity.application.service.interfaces.AuthorActivityTimeService;
import io.github.mirvmir.activity.web.request.*;
import io.github.mirvmir.activity.web.response.*;
import io.github.mirvmir.common.annotation.RequiresCompletedProfile;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RequiresCompletedProfile
@RestController
@RequestMapping("/author/activities")
@PreAuthorize("hasAuthority('ROLE_USER')")
public class AuthorActivityController {

    private final AuthorActivityService authorActivityService;
    private final ActivitySlotService activitySlotService;
    private final AuthorActivityTimeService authorActivityTimeService;

    @GetMapping("/{activityId}")
    public AuthorActivityDescriptionResponse getByIdForAuthor(
            @PathVariable("activityId")
            Long id
    ) {
        return authorActivityService.getActivityByAuthor(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IdResponse createActivity(
            @Valid
            @RequestBody
            CreateActivityRequest request
    ) {
        return authorActivityService.createActivity(request);
    }

    @PatchMapping("/{activityId}")
    public ActivityResponse updateActivity(
            @PathVariable("activityId")
            Long activityId,
            @RequestBody
            UpdateActivityRequest request
    ) {
        return authorActivityService.updateActivity(
                activityId,
                request
        );
    }

    @PatchMapping("/slots/{activitySlotId}/room-join-url")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateSlotRoomJoinUrl(
            @PathVariable("activitySlotId")
            Long activitySlotId,
            @Valid
            @RequestBody
            UpdateActivitySlotRoomJoinUrlRequest request
    ) {
        activitySlotService.updateRoomJoinUrl(
                activitySlotId,
                request
        );
    }

    @PostMapping("/{activityId}/publish")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void publish(
            @PathVariable("activityId")
            Long activityId
    ) {
        authorActivityService.publish(activityId);
    }

    @PostMapping("/{activityId}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archive(
            @PathVariable("activityId")
            Long activityId
    ) {
        authorActivityService.archive(activityId);
    }

    @PostMapping("/{activityId}/unarchive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unarchive(
            @PathVariable("activityId")
            Long activityId
    ) {
        authorActivityService.unarchive(activityId);
    }

    @DeleteMapping("/{activityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteActivity(
            @PathVariable("activityId")
            Long activityId
    ) {
        authorActivityService.deleteActivity(activityId);
    }

    @PatchMapping("/{activityId}/topics")
    public void updateTopics(
            @PathVariable("activityId")
            Long activitySlotId,
            @RequestBody
            UpdateActivityTopicsRequest request
    ) {
        activitySlotService.updateTopics(
                activitySlotId,
                request
        );
    }

    @PostMapping("/{activitySlotId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelSlot(
            @PathVariable("activitySlotId")
            Long activitySlotId,
            @Valid
            @RequestBody
            CancelActivitySlotRequest request
    ) {
        activitySlotService.cancelByAuthor(activitySlotId, request);
    }

    @PostMapping("/{activitySlotId}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void completeSlot(
            @PathVariable("activitySlotId")
            Long activitySlotId
    ) {
        activitySlotService.complete(activitySlotId);
    }

    @PostMapping("/{activityId}/slots")
    public ActivitySlotResponse createGroupSlot(
            @PathVariable("activityId")
            Long activityId,
            @RequestBody
            CreateGroupActivitySlotRequest request
    ) {
        return authorActivityService.createGroupSlot(
                activityId,
                request
        );
    }

    @PostMapping("/{activityId}/availability-times")
    public ActivityTimeResponse createAvailabilityTime(
            @PathVariable("activityId")
            Long activityId,
            @RequestBody
            CreateAvailabilityTimeRequest request
    ) {
        return authorActivityTimeService.createAvailabilityTime(
                activityId,
                request
        );
    }
}
