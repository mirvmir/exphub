package io.github.mirvmir.practice.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class PracticeSubmissionAnswer {
    private Long id;
    @NonNull
    private Long practiceSubmissionId;
    @NonNull
    private String html;
    private Long fileId;
    @NonNull
    private Instant createdAt;

    public static PracticeSubmissionAnswer createByStudent(
            Long practiceSubmissionId,
            String html,
            Long fileId,
            Instant now
    ) {
        return new PracticeSubmissionAnswer(
                null,
                practiceSubmissionId,
                html,
                fileId,
                now
        );
    }

    public static PracticeSubmissionAnswer load(
            Long id,
            Long practiceSubmissionId,
            String html,
            Long fileId,
            Instant createdAt
    ) {
        return new PracticeSubmissionAnswer(
                id,
                practiceSubmissionId,
                html,
                fileId,
                createdAt
        );
    }

    public void assignId(Long id) {
        this.id = id;
    }
}