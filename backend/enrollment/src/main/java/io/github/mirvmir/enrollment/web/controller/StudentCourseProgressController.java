package io.github.mirvmir.enrollment.web.controller;

import io.github.mirvmir.common.annotation.RequiresCompletedProfile;
import io.github.mirvmir.enrollment.application.service.interfaces.CourseProgressService;
import io.github.mirvmir.enrollment.web.response.CourseProgressResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RequiresCompletedProfile
@RestController
@RequestMapping("/student/courses")
@PreAuthorize("hasAuthority('ROLE_USER')")
public class StudentCourseProgressController {

    private final CourseProgressService courseProgressService;

    @PostMapping("/{courseId}/lessons/{courseLessonId}/complete")
    public CourseProgressResponse completeLesson(
            @PathVariable("courseId")
            Long courseId,
            @PathVariable("courseLessonId")
            Long courseLessonId
    ) {
        return courseProgressService.completeLesson(
                courseId,
                courseLessonId
        );
    }
}