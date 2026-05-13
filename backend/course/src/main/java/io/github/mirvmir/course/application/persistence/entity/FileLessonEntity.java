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
@Table(name = "lesson_pdf")
public class FileLessonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_block_id", nullable = false)
    private LessonBlockEntity lessonBlock;

    @Column(name = "file_asset_id", nullable = false)
    private Long fileAssetId;

    public void assignLessonBlock(LessonBlockEntity lessonBlock) {
        this.lessonBlock = lessonBlock;
    }
}