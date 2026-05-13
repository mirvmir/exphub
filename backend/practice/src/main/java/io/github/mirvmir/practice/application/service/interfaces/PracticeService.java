package io.github.mirvmir.practice.application.service.interfaces;

import io.github.mirvmir.practice.web.request.CreatePracticeAnswerRequest;
import io.github.mirvmir.practice.web.request.CreatePracticeCommentRequest;
import io.github.mirvmir.practice.web.response.PracticeAnswerResponse;
import io.github.mirvmir.practice.web.response.PracticeCommentResponse;
import io.github.mirvmir.practice.web.response.PracticeSubmissionDetailsResponse;
import io.github.mirvmir.practice.web.response.PracticeSubmissionResponse;

import java.util.List;

public interface PracticeService {
    PracticeAnswerResponse addAnswer(Long courseLessonId, CreatePracticeAnswerRequest request);
    PracticeCommentResponse addComment(Long answerId, CreatePracticeCommentRequest request);
    PracticeSubmissionResponse checkSubmissionByTeacher(Long courseLessonId);
    PracticeSubmissionDetailsResponse getMySubmission(Long courseLessonId);
    List<PracticeSubmissionDetailsResponse> getLessonSubmissionsForTeacher(Long courseLessonId);
}
