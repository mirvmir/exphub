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
    private String text;
    private Long fileId;
    @NonNull
    private Instant createdAt;

    public static PracticeSubmissionAnswer createByStudent(
            Long practiceSubmissionId,
            String text,
            Long fileId,
            Instant now
    ) {
        return new PracticeSubmissionAnswer(
                null,
                practiceSubmissionId,
                text,
                fileId,
                now
        );
    }

    public static PracticeSubmissionAnswer load(
            Long id,
            Long practiceSubmissionId,
            String text,
            Long fileId,
            Instant createdAt
    ) {
        return new PracticeSubmissionAnswer(
                id,
                practiceSubmissionId,
                text,
                fileId,
                createdAt
        );
    }

    public void assignId(Long id) {
        this.id = id;
    }
}