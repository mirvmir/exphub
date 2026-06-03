package io.github.mirvmir.course.web.controller;

import io.github.mirvmir.course.web.response.CourseInfoResponse;
import io.github.mirvmir.course.application.service.interfaces.CourseService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/{id}")
    public CourseInfoResponse getById(
            @PathVariable("id")
            Long id
    ) {
        return courseService.getCourse(id);
    }
}
