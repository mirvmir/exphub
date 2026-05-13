package io.github.mirvmir.activity.web.controller;

import io.github.mirvmir.activity.web.response.ActivityDescriptionResponse;
import io.github.mirvmir.activity.application.service.interfaces.ActivityService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/activities")
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping("/{activityId}")
    public ActivityDescriptionResponse getById(
            @PathVariable("activityId")
            Long activityId
    ) {
        return activityService.getActivity(activityId);
    }
}
