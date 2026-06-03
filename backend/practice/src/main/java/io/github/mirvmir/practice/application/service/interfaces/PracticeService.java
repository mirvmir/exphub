package io.github.mirvmir.practice.application.service.interfaces;

import io.github.mirvmir.practice.web.request.CreatePracticeAnswerRequest;
import io.github.mirvmir.practice.web.request.CreatePracticeCommentRequest;
import io.github.mirvmir.practice.web.response.PracticeAnswerResponse;
import io.github.mirvmir.practice.web.response.PracticeCommentResponse;
import io.github.mirvmir.practice.web.response.PracticeSubmissionDetailsResponse;
import io.github.mirvmir.practice.web.response.PracticeSubmissionResponse;

import java.util.List;
import java.util.UUID;

public interface PracticeService {
    PracticeAnswerResponse addAnswer(UUID stableLessonId, CreatePracticeAnswerRequest request);
    PracticeCommentResponse addComment(Long answerId, CreatePracticeCommentRequest request);
    PracticeSubmissionResponse checkSubmissionByTeacher(Long practiceSubmissionId);
    PracticeSubmissionDetailsResponse getMySubmission(UUID stableLessonId);
    List<PracticeSubmissionDetailsResponse> getLessonSubmissionsForTeacher(UUID stableLessonId);
}
