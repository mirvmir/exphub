package io.github.mirvmir.course.domain.content;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.course.exception.CourseErrorCode;
import lombok.Getter;

@Getter
public final class LessonVideo implements LessonContent {
    private final Long videoAssetId;

    public static LessonVideo create(Long videoAssetId) {
        return new LessonVideo(videoAssetId);
    }

    public static LessonVideo load(Long videoAssetId) {
        return new LessonVideo(videoAssetId);
    }

    private LessonVideo(Long videoAssetId) {
        if (videoAssetId == null || videoAssetId <= 0) {
            throw new BusinessException(CourseErrorCode.LESSON_BLOCK_VIDEO_REQUIRED);
        }

        this.videoAssetId = videoAssetId;
    }

    @Override
    public LessonContentType type() {
        return LessonContentType.VIDEO;
    }
}