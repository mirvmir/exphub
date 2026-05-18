package io.github.mirvmir.course.web.controller;

import io.github.mirvmir.common.annotation.RequiresCompletedProfile;
import io.github.mirvmir.course.application.service.interfaces.AuthorCourseService;
import io.github.mirvmir.course.web.request.*;
import io.github.mirvmir.course.web.response.AuthorCourseLessonResponse;
import io.github.mirvmir.course.web.response.AuthorCourseModuleResponse;
import io.github.mirvmir.course.web.response.AuthorCourseResponse;
import io.github.mirvmir.course.web.response.IdResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RequiresCompletedProfile
@RestController
@RequestMapping("/author/courses")
@PreAuthorize("hasAuthority('ROLE_USER')")
public class AuthorCourseController {

    private final AuthorCourseService authorCourseService;

    @GetMapping
    public List<AuthorCourseResponse> getAllCourse() {
        return authorCourseService.getAllCourse();
    }


    @GetMapping("/{courseId}")
    public AuthorCourseResponse getCourse(
            @PathVariable("courseId")
            Long courseId
    ) {
        return authorCourseService.getCourse(courseId);
    }

    @GetMapping("/{courseId}/modules/{stableModuleId}")
    public AuthorCourseModuleResponse getModule(
            @PathVariable("courseId")
            Long courseId,
            @PathVariable("stableModuleId")
            UUID stableModuleId
    ) {
        return authorCourseService.getModule(
                courseId,
                stableModuleId
        );
    }

    @GetMapping("/{courseId}/lessons/{stableLessonId}")
    public AuthorCourseLessonResponse getLesson(
            @PathVariable("courseId")
            Long courseId,
            @PathVariable("stableLessonId")
            UUID stableLessonId
    ) {
        return authorCourseService.getLesson(
                courseId,
                stableLessonId
        );
    }

    @PostMapping
    public IdResponse createCourse(
            @Valid
            @RequestBody
            CreateCourseRequest request
    ) {
        return authorCourseService.createCourse(request);
    }

    @PatchMapping("/{courseId}/topics")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateTopics(
            @PathVariable("courseId")
            Long courseId,
            @Valid
            @RequestBody
            UpdateCourseTopicsRequest request
    ) {
        authorCourseService.updateTopics(courseId, request);
    }

    @PatchMapping("/{courseId}/lessons/{stableLessonId}/opens-at")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateLessonOpensAt(
            @PathVariable("courseId")
            Long courseId,
            @PathVariable("stableLessonId")
            UUID stableLessonId,
            @Valid
            @RequestBody
            UpdateLessonOpensAtRequest request
    ) {
        authorCourseService.updateLessonOpensAt(courseId, stableLessonId, request);
    }

    @PatchMapping("/{courseId}/draft")
    public AuthorCourseResponse updateDraftCourse(
            @PathVariable("courseId")
            Long courseId,
            @Valid
            @RequestBody
            UpdateCourseDraftRequest request
    ) {
        return authorCourseService.updateDraftCourse(courseId, request);
    }

    @PutMapping("/{courseId}/draft/modules")
    public AuthorCourseResponse saveDraftModules(
            @PathVariable("courseId")
            Long courseId,
            @Valid
            @RequestBody
            SaveDraftModulesRequest request
    ) {
        return authorCourseService.saveDraftModules(courseId, request);
    }

    @PutMapping("/{courseId}/draft/modules/{moduleId}/lessons")
    public AuthorCourseModuleResponse saveDraftModuleLessons(
            @PathVariable("courseId")
            Long courseId,
            @PathVariable("moduleId")
            Long moduleId,
            @Valid
            @RequestBody
            SaveDraftModuleLessonsRequest request
    ) {
        return authorCourseService.saveDraftModuleLessons(courseId, moduleId, request);
    }

    @PutMapping("/{courseId}/draft/lessons/{lessonId}/blocks")
    public AuthorCourseLessonResponse saveDraftLessonBlocks(
            @PathVariable("courseId")
            Long courseId,
            @PathVariable("lessonId")
            Long lessonId,
            @Valid
            @RequestBody
            SaveDraftLessonBlocksRequest request
    ) {
        return authorCourseService.saveDraftLessonBlocks(courseId, lessonId, request);
    }

    @PostMapping("/{courseId}/publication-request")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestPublication(
            @PathVariable("courseId")
            Long courseId
    ) {
        authorCourseService.requestPublication(courseId);
    }

    @PostMapping("/{courseId}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archive(
            @PathVariable("courseId")
            Long courseId
    ) {
        authorCourseService.archive(courseId);
    }

    @PostMapping("/{courseId}/unarchive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unarchive(
            @PathVariable("courseId")
            Long courseId
    ) {
        authorCourseService.unarchive(courseId);
    }

    @DeleteMapping("/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourse(
            @PathVariable("courseId")
            Long courseId
    ) {
        authorCourseService.deleteCourse(courseId);
    }
}