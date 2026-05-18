package io.github.mirvmir.practice.application.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "practice_submission_comment")
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
public class PracticeSubmissionCommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "practice_submission_answer_id", nullable = false)
    private Long practiceSubmissionAnswerId;

    @Column(name = "text", nullable = false, columnDefinition = "text")
    private String text;

    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}