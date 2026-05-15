package io.github.mirvmir.course.domain;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.course.domain.content.FileContentData;
import io.github.mirvmir.course.domain.content.HtmlContentData;
import io.github.mirvmir.course.domain.content.LessonContentData;
import io.github.mirvmir.course.domain.content.VideoContentData;
import io.github.mirvmir.course.exception.CourseErrorCode;
import io.github.mirvmir.course.web.request.SaveDraftLessonBlockItemRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class CourseLesson {
    private Long id;
    private UUID stableLessonId;
    private String title;
    private LessonType type;
    private Integer sortOrder;
    private List<LessonBlock> blocks;

    public static CourseLesson create(String title,
                                      LessonType type) {
        return new CourseLesson(
                null,
                UUID.randomUUID(),
                title,
                type,
                null,
                new ArrayList<>()
        );
    }

    public static CourseLesson copyToDraftFromPublished(CourseLesson source) {
        return new CourseLesson(
                null,
                source.getStableLessonId(),
                source.getTitle(),
                source.getType(),
                source.getSortOrder(),
                source.getBlocks()
                        .stream()
                        .sorted(Comparator.comparing(
                                LessonBlock::getSortOrder,
                                Comparator.nullsLast(Integer::compareTo)
                        ))
                        .map(LessonBlock::copyToDraftFromPublished)
                        .collect(Collectors.toCollection(ArrayList::new))
        );
    }

    public static CourseLesson load(Long id,
                                    UUID stableLessonId,
                                    String title,
                                    LessonType type,
                                    Integer sortOrder,
                                    List<LessonBlock> blocks) {
        return new CourseLesson(
                id,
                stableLessonId,
                title,
                type,
                sortOrder,
                blocks == null ? new ArrayList<>() : new ArrayList<>(blocks)
        );
    }

    public void update(String title,
                       LessonType type) {
        this.title = title;
        this.type = type;
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void saveBlocks(List<SaveDraftLessonBlockItemRequest> blockRequests) {
        if (blockRequests == null) {
            this.blocks.clear();
            return;
        }

        Map<Long, LessonBlock> existingById = blocks.stream()
                .filter(block -> block.getId() != null)
                .collect(Collectors.toMap(
                        LessonBlock::getId,
                        Function.identity()
                ));

        List<LessonBlock> result = new ArrayList<>();

        for (int i = 0; i < blockRequests.size(); i++) {
            SaveDraftLessonBlockItemRequest request = blockRequests.get(i);

            LessonBlock block;
            LessonContentData contentData = toContentData(request);

            if (request.id() == null) {
                block = LessonBlock.create(contentData);
            } else {
                block = existingById.get(request.id());

                if (block == null) {
                    throw new NotFoundException(
                            CourseErrorCode.LESSON_BLOCK_NOT_FOUND,
                            "Lesson block with id=" + request.id() + " not found in draft lesson"
                    );
                }

                block.updateContent(contentData);
            }

            block.updateSortOrder(resolveSortOrder(request.sortOrder(), i));

            result.add(block);
        }

        this.blocks.clear();
        this.blocks.addAll(result);
    }

    private LessonContentData toContentData(SaveDraftLessonBlockItemRequest request) {
        if (request.type() == null) {
            throw new BusinessException(CourseErrorCode.LESSON_BLOCK_TYPE_REQUIRED);
        }

        return switch (request.type()) {
            case HTML -> {
                if (request.html() == null || request.html().isBlank()) {
                    throw new BusinessException(
                            CourseErrorCode.LESSON_BLOCK_HTML_REQUIRED
                    );
                }

                String sanitizedHtml = HtmlSanitizer.sanitize(request.html());
                if (sanitizedHtml.isBlank()) {
                    throw new BusinessException(
                            CourseErrorCode.INVALID_LESSON_BLOCK_HTML
                    );
                }

                yield new HtmlContentData(sanitizedHtml);
            }
            case FILE -> {
                if (request.fileAssetId() == null) {
                    throw new BusinessException(
                            CourseErrorCode.LESSON_BLOCK_FILE_REQUIRED
                    );
                }

                yield new FileContentData(request.fileAssetId());
            }
            case VIDEO -> {
                if (request.fileAssetId() == null) {
                    throw new BusinessException(
                            CourseErrorCode.LESSON_BLOCK_VIDEO_REQUIRED
                    );
                }

                yield new VideoContentData(request.videoAssetId());
            }
        };
    }

    public void validateBeforePublication() {
        if (title == null || title.isBlank()) {
            throw new BusinessException(CourseErrorCode.COURSE_LESSON_TITLE_REQUIRED);
        }

        if (blocks == null || blocks.isEmpty()) {
            throw new BusinessException(CourseErrorCode.COURSE_LESSON_BLOCKS_REQUIRED);
        }

        for (LessonBlock block : blocks) {
            block.validateBeforePublication();
        }
    }

    public boolean isPublishable() {
        return !(title == null || title.isBlank())
                && !(blocks == null || blocks.isEmpty())
                && blocks.stream().allMatch(block -> block.isPublishable());
    }

    private Integer resolveSortOrder(Integer requestSortOrder,
                                     int index) {
        return requestSortOrder == null
                ? index
                : requestSortOrder;
    }
}