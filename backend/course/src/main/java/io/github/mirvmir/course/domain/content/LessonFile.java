package io.github.mirvmir.course.domain.content;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.course.exception.CourseErrorCode;
import lombok.Getter;

@Getter
public final class LessonFile implements LessonContent {
    private final Long fileAssetId;

    public static LessonFile create(Long fileAssetId) {
        return new LessonFile(fileAssetId);
    }

    public static LessonFile load(Long fileAssetId) {
        return new LessonFile(fileAssetId);
    }

    private LessonFile(Long fileAssetId) {
        if (fileAssetId == null || fileAssetId <= 0) {
            throw new BusinessException(CourseErrorCode.LESSON_BLOCK_FILE_REQUIRED);
        }

        this.fileAssetId = fileAssetId;
    }

    @Override
    public LessonContentType type() {
        return LessonContentType.FILE;
    }
}