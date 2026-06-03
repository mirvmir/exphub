package io.github.mirvmir.practice.web.controller;

import io.github.mirvmir.common.annotation.RequiresCompletedProfile;
import io.github.mirvmir.practice.application.service.interfaces.PracticeService;
import io.github.mirvmir.practice.web.request.CreatePracticeAnswerRequest;
import io.github.mirvmir.practice.web.request.CreatePracticeCommentRequest;
import io.github.mirvmir.practice.web.response.PracticeAnswerResponse;
import io.github.mirvmir.practice.web.response.PracticeCommentResponse;
import io.github.mirvmir.practice.web.response.PracticeSubmissionDetailsResponse;
import io.github.mirvmir.practice.web.response.PracticeSubmissionResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@RequiresCompletedProfile
@RestController
@RequestMapping
@PreAuthorize("hasAuthority('ROLE_USER')")
public class PracticeController {

    private final PracticeService practiceService;

    @PostMapping("/student/practice/lessons/{stableLessonId}/answers")
    public PracticeAnswerResponse addAnswerByStudent(
            @PathVariable("stableLessonId")
            UUID stableLessonId,
            @Valid
            @RequestBody
            CreatePracticeAnswerRequest request
    ) {
        return practiceService.addAnswer(stableLessonId, request);
    }

    @PostMapping("/author/practice/answers/{answerId}/comments")
    public PracticeCommentResponse addCommentByTeacher(
            @PathVariable("answerId")
            Long answerId,
            @Valid
            @RequestBody
            CreatePracticeCommentRequest request
    ) {
        return practiceService.addComment(
                answerId,
                request
        );
    }

    @PostMapping("/author/practice/submissions/{practiceSubmissionId}/check")
    public PracticeSubmissionResponse checkSubmissionByTeacher(
            @PathVariable("practiceSubmissionId")
            Long practiceSubmissionId
    ) {
        return practiceService.checkSubmissionByTeacher(practiceSubmissionId);
    }

    @GetMapping("/student/practice/lessons/{stableLessonId}/submission")
    public PracticeSubmissionDetailsResponse getMySubmission(
            @PathVariable("stableLessonId")
            UUID stableLessonId
    ) {
        return practiceService.getMySubmission(stableLessonId);
    }

    @GetMapping("/teacher/practice/lessons/{stableLessonId}/submissions")
    public List<PracticeSubmissionDetailsResponse> getLessonSubmissionsForTeacher(
            @PathVariable("stableLessonId")
            UUID stableLessonId
    ) {
        return practiceService.getLessonSubmissionsForTeacher(stableLessonId);
    }
}