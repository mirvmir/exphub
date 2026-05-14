package io.github.mirvmir.course.web.controller;

import io.github.mirvmir.course.application.service.interfaces.ModerationCourseService;
import io.github.mirvmir.course.web.request.RejectCourseRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/admin/courses")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class ModerationCourseController {

    private final ModerationCourseService moderationService;

    @PostMapping("/{courseId}/approve")
    public void approve(
            @PathVariable("courseId")
            Long courseId
    ) {
        moderationService.approve(courseId);
    }

    @PostMapping("/{courseId}/reject")
    public void reject(
            @PathVariable("courseId")
            Long courseId,
            @Valid
            @RequestBody
            RejectCourseRequest request
    ) {
        moderationService.reject(courseId, request);
    }

    @PostMapping("/{courseId}/block")
    public void block(
            @PathVariable("courseId")
            Long courseId
    ) {
        moderationService.block(courseId);
    }
}