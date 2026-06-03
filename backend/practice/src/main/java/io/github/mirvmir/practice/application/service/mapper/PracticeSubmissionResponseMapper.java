package io.github.mirvmir.practice.application.service.mapper;

import io.github.mirvmir.practice.domain.PracticeSubmission;
import io.github.mirvmir.practice.domain.PracticeSubmissionAnswer;
import io.github.mirvmir.practice.domain.PracticeSubmissionComment;
import io.github.mirvmir.practice.web.response.PracticeAnswerDetailsResponse;
import io.github.mirvmir.practice.web.response.PracticeCommentResponse;
import io.github.mirvmir.practice.web.response.PracticeSubmissionDetailsResponse;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface PracticeSubmissionResponseMapper {

    PracticeSubmissionDetailsResponse toSubmissionResponse(
            PracticeSubmission submission,
            List<PracticeAnswerDetailsResponse> answers
    );

    default PracticeAnswerDetailsResponse toAnswerResponse(
            PracticeSubmissionAnswer answer,
            List<PracticeSubmissionComment> comments
    ) {
        return new PracticeAnswerDetailsResponse(
                answer.getId(),
                answer.getPracticeSubmissionId(),
                answer.getText(),
                answer.getFileId(),
                answer.getCreatedAt(),
                comments.stream()
                        .map(this::toCommentResponse)
                        .toList()
        );
    }

    default PracticeCommentResponse toCommentResponse(
            PracticeSubmissionComment comment
    ) {
        return new PracticeCommentResponse(
                comment.getId(),
                comment.getPracticeSubmissionAnswerId(),
                comment.getText(),
                comment.getFileId(),
                comment.getCreatedAt()
        );
    }

    default List<PracticeSubmissionDetailsResponse> toSubmissionResponses(
            List<PracticeSubmission> submissions,
            Map<Long, List<PracticeSubmissionAnswer>> answersBySubmissionId,
            Map<Long, List<PracticeSubmissionComment>> commentsByAnswerId
    ) {
        return submissions.stream()
                .map(submission -> {
                    List<PracticeAnswerDetailsResponse> answers =
                            answersBySubmissionId
                                    .getOrDefault(submission.getId(), List.of())
                                    .stream()
                                    .map(answer -> toAnswerResponse(
                                            answer,
                                            commentsByAnswerId.getOrDefault(
                                                    answer.getId(),
                                                    List.of()
                                            )
                                    ))
                                    .toList();

                    return toSubmissionResponse(submission, answers);
                })
                .toList();
    }
}