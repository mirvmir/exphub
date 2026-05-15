package io.github.mirvmir.course.domain.content;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.course.exception.CourseErrorCode;
import lombok.Getter;

@Getter
public final class LessonFile implements LessonContent {
    private final Long id;
    private final Long fileAssetId;

    public static LessonFile create(Long fileAssetId) {
        return new LessonFile(null, fileAssetId);
    }

    public static LessonFile load(Long id, Long fileAssetId) {
        return new LessonFile(id, fileAssetId);
    }

    private LessonFile(Long id, Long fileAssetId) {
        if (fileAssetId == null || fileAssetId <= 0) {
            throw new BusinessException(CourseErrorCode.LESSON_BLOCK_FILE_REQUIRED);
        }

        this.id = id;
        this.fileAssetId = fileAssetId;
    }

    @Override
    public LessonContentType type() {
        return LessonContentType.FILE;
    }

    @Override
    public LessonContent copyToDraft() {
        return LessonFile.create(this.fileAssetId);
    }
}