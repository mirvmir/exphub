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
    private String html;
    private Long fileId;
    @NonNull
    private Instant createdAt;

    public static PracticeSubmissionComment createByTeacher(
            Long practiceSubmissionAnswerId,
            String html,
            Long fileId,
            Instant now
    ) {
        return new PracticeSubmissionComment(
                null,
                practiceSubmissionAnswerId,
                html,
                fileId,
                now
        );
    }

    public static PracticeSubmissionComment load(
            Long id,
            Long practiceSubmissionAnswerId,
            String html,
            Long fileId,
            Instant createdAt
    ) {
        return new PracticeSubmissionComment(
                id,
                practiceSubmissionAnswerId,
                html,
                fileId,
                createdAt
        );
    }

    public void assignId(Long id) {
        this.id = id;
    }
}