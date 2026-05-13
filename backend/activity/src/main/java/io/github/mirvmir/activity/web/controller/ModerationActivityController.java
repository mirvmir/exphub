package io.github.mirvmir.activity.web.controller;

import io.github.mirvmir.activity.application.service.interfaces.ModerationActivityService;
import io.github.mirvmir.activity.web.request.RejectActivityRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/moderation/activities")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class ModerationActivityController {

    private final ModerationActivityService moderationActivityService;

    @PostMapping("/{activityId}/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approve(
            @PathVariable("activityId")
            Long activityId
    ) {
        moderationActivityService.approve(activityId);
    }

    @PostMapping("/{activityId}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(
            @PathVariable("activityId")
            Long activityId,
            @RequestBody
            RejectActivityRequest request
    ) {
        moderationActivityService.reject(activityId, request);
    }

    @PostMapping("/{activityId}/block")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void block(
            @PathVariable("activityId")
            Long activityId
    ) {
        moderationActivityService.block(activityId);
    }
}
