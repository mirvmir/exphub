package io.github.mirvmir.activity.web.controller;

import io.github.mirvmir.activity.application.service.interfaces.ActivitySlotService;
import io.github.mirvmir.activity.web.request.CancelActivitySlotRequest;
import io.github.mirvmir.common.annotation.RequiresCompletedProfile;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequiresCompletedProfile
@RequestMapping("/student/activities")
@PreAuthorize("hasAuthority('ROLE_USER')")
public class StudentActivityController {

    private final ActivitySlotService activitySlotService;

    @PostMapping("/{activitySlotId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelIndividualActivitySlot(
            @PathVariable("activitySlotId")
            Long activitySlotId,
            @Valid
            @RequestBody
            CancelActivitySlotRequest request
    ) {
        activitySlotService.cancelByStudent(
                activitySlotId,
                request
        );
    }
}