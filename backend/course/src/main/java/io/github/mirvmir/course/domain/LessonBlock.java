package io.github.mirvmir.course.domain;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.course.domain.content.*;
import io.github.mirvmir.course.exception.CourseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class LessonBlock {

    private Long id;
    private UUID stableBlockId;
    private LessonContent content;
    private Integer sortOrder;
    private String contentHash;

    public static LessonBlock create(LessonContentData contentData) {
        LessonContent content = toContent(contentData);

        LessonBlock block = new LessonBlock(
                null,
                UUID.randomUUID(),
                content,
                null,
                null
        );

        block.recalculateContentHash();

        return block;
    }

    public static LessonBlock load(
            Long id,
            UUID stableBlockId,
            LessonContent content,
            Integer sortOrder,
            String contentHash
    ) {
        return new LessonBlock(
                id,
                stableBlockId,
                content,
                sortOrder,
                contentHash
        );
    }

    public static LessonBlock copyToDraftFromPublished(LessonBlock source) {
        return new LessonBlock(
                null,
                source.getStableBlockId(),
                source.getContent(),
                source.getSortOrder(),
                source.getContentHash()
        );
    }

    public void updateContent(LessonContentData contentData) {
        this.content = toContent(contentData);
        recalculateContentHash();
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean hasSameContentAs(LessonBlock other) {
        return other != null
                && Objects.equals(stableBlockId, other.getStableBlockId())
                && Objects.equals(contentHash, other.getContentHash());
    }

    public void validateBeforePublication() {
        if (content == null || content.type() == null) {
            throw new BusinessException(CourseErrorCode.LESSON_BLOCK_TYPE_REQUIRED);
        }
    }

    public LessonContentType getType() {
        return content == null ? null : content.type();
    }

    public String getHtml() {
        if (content instanceof LessonHtml lessonHtml) {
            return lessonHtml.getHtml();
        }

        return null;
    }

    public Long getFileAssetId() {
        if (content instanceof LessonFile lessonFile) {
            return lessonFile.getFileAssetId();
        }

        return null;
    }

    public Long getVideoAssetId() {
        if (content instanceof LessonVideo lessonVideo) {
            return lessonVideo.getVideoAssetId();
        }

        return null;
    }

    private static LessonContent toContent(LessonContentData contentData) {
        if (contentData == null || contentData.type() == null) {
            throw new BusinessException(CourseErrorCode.LESSON_BLOCK_TYPE_REQUIRED);
        }

        return switch (contentData.type()) {
            case HTML -> {
                HtmlContentData data = (HtmlContentData) contentData;
                yield LessonHtml.create(data.html());
            }
            case FILE -> {
                FileContentData data = (FileContentData) contentData;
                yield LessonFile.create(data.fileAssetId());
            }
            case VIDEO -> {
                VideoContentData data = (VideoContentData) contentData;
                yield LessonVideo.create(data.videoAssetId());
            }
        };
    }

    private void recalculateContentHash() {
        this.contentHash = LessonBlockHashCalculator.calculate(content);
    }
}