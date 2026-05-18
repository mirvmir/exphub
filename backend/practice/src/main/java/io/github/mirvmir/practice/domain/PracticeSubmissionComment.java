package io.github.mirvmir.practice.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class PracticeSubmissionComment {
    private Long id;
    @NonNull
    private Long practiceSubmissionAnswerId;
    @NonNull
    private String text;
    private Long fileId;
    @NonNull
    private Instant createdAt;

    public static PracticeSubmissionComment createByTeacher(
            Long practiceSubmissionAnswerId,
            String text,
            Long fileId,
            Instant now
    ) {
        return new PracticeSubmissionComment(
                null,
                practiceSubmissionAnswerId,
                text,
                fileId,
                now
        );
    }

    public static PracticeSubmissionComment load(
            Long id,
            Long practiceSubmissionAnswerId,
            String text,
            Long fileId,
            Instant createdAt
    ) {
        return new PracticeSubmissionComment(
                id,
                practiceSubmissionAnswerId,
                text,
                fileId,
                createdAt
        );
    }

    public void assignId(Long id) {
        this.id = id;
    }
}