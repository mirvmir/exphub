package io.github.mirvmir.course.web.controller;

import io.github.mirvmir.common.annotation.RequiresCompletedProfile;
import io.github.mirvmir.course.application.service.interfaces.StudentCourseService;
import io.github.mirvmir.course.web.response.*;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@AllArgsConstructor
@RequiresCompletedProfile
@RestController
@RequestMapping("/student/courses")
@PreAuthorize("hasAuthority('ROLE_USER')")
public class StudentCourseController {

    private final StudentCourseService studentCourseService;

    @GetMapping("/{courseId}/description")
    public CourseInfoResponse getCourseDescription(
            @PathVariable("courseId")
            Long courseId
    ) {
        return studentCourseService.getCourseDescription(courseId);
    }

    @GetMapping("/{courseId}")
    public StudentCourseResponse getCourse(
            @PathVariable("courseId")
            Long courseId
    ) {
        return studentCourseService.getCourse(courseId);
    }

    @GetMapping("/{courseId}/modules/{stableModuleId}")
    public StudentCourseModuleResponse getModule(
            @PathVariable("courseId")
            Long courseId,
            @PathVariable("stableModuleId")
            UUID stableModuleId
    ) {
        return studentCourseService.getModule(
                courseId,
                stableModuleId
        );
    }

    @GetMapping("/{courseId}/lessons/{stableLessonId}")
    public StudentCourseLessonResponse getLesson(
            @PathVariable("courseId")
            Long courseId,
            @PathVariable("stableLessonId")
            UUID stableLessonId
    ) {
        return studentCourseService.getLesson(
                courseId,
                stableLessonId
        );
    }
}
