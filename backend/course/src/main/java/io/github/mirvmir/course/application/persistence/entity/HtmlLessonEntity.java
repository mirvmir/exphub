package io.github.mirvmir.course.application.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Getter
@Entity
@Table(name = "lesson_html")
public class HtmlLessonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_block_id", nullable = false)
    private LessonBlockEntity lessonBlock;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    public void assignLessonBlock(LessonBlockEntity lessonBlock) {
        this.lessonBlock = lessonBlock;
    }
}