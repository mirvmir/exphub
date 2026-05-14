package io.github.mirvmir.course.domain;

import io.github.mirvmir.common.domain.ContentStatus;
import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.course.exception.CourseErrorCode;
import io.github.mirvmir.course.web.request.SaveDraftLessonBlockItemRequest;
import io.github.mirvmir.course.web.request.SaveDraftLessonItemRequest;
import io.github.mirvmir.course.web.request.SaveDraftModuleItemRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class Course {

    private Long id;
    private Long authorId;
    private ContentStatus status;
    private Long subjectId;
    private Set<Long> topicIds;
    private Set<CourseLessonOpening> lessonOpenings;
    private CourseVersion publishedVersion;
    private CourseVersion draftVersion;

    public static Course create(Long authorId,
                                String title) {
        if (title == null || title.isBlank()) {
            throw new BusinessException(CourseErrorCode.COURSE_TITLE_REQUIRED);
        }

        return new Course(
                null,
                authorId,
                ContentStatus.DRAFT,
                null,
                new HashSet<>(),
                new HashSet<>(),
                null,
                CourseVersion.createDraft(title)
        );
    }

    public static Course load(Long id,
                              Long authorId,
                              ContentStatus status,
                              Long subjectId,
                              Set<Long> topicIds,
                              Set<CourseLessonOpening> lessonOpenings,
                              CourseVersion publishedVersion,
                              CourseVersion draftVersion) {
        return new Course(
                id,
                authorId,
                status,
                subjectId,
                topicIds == null ? new HashSet<>() : new HashSet<>(topicIds),
                lessonOpenings == null ? new HashSet<>() : new HashSet<>(lessonOpenings),
                publishedVersion,
                draftVersion
        );
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public void updateTopics(Set<Long> topicIds, Long subjectId) {
        ensureNotDeleted();
        ensureNotBlocked();

        this.subjectId = subjectId;
        this.topicIds = topicIds == null
                ? new HashSet<>()
                : new HashSet<>(topicIds);
    }

    public void updateDraftCourse(String title,
                                  String shortDescription,
                                  String descriptionHtml,
                                  BigDecimal priceAmount,
                                  Currency priceCurrency) {
        ensureCanEditDraft();

        draftVersion.updateCourseInfo(
                title,
                shortDescription,
                descriptionHtml,
                priceAmount,
                priceCurrency
        );
    }

    public void saveDraftModules(List<SaveDraftModuleItemRequest> modules) {
        ensureCanEditDraft();

        draftVersion.saveModules(modules);
    }

    public void saveDraftModuleLessons(Long moduleId,
                                       List<SaveDraftLessonItemRequest> lessons) {
        ensureCanEditDraft();

        draftVersion.saveModuleLessons(
                moduleId,
                lessons
        );
    }

    public void saveDraftLessonBlocks(Long lessonId,
                                      List<SaveDraftLessonBlockItemRequest> blocks) {
        ensureCanEditDraft();

        draftVersion.saveLessonBlocks(
                lessonId,
                blocks
        );
    }

    public void requestPublication() {
        ensureCanEditDraft();

        draftVersion.validateBeforePublication();
        draftVersion.sendToModeration();
    }

    public void approveModeration() {
        ensureNotDeleted();
        ensureNotBlocked();

        draftVersion.approveModeration();

        this.publishedVersion = draftVersion;
        this.draftVersion = CourseVersion.copyToDraftFromPublished(publishedVersion);
        this.status = ContentStatus.ACTIVE;
    }

    public void rejectModeration(String moderationComment) {
        ensureNotDeleted();
        ensureNotBlocked();

        draftVersion.rejectModeration(moderationComment);
    }

    public void archive() {
        ensureNotDeleted();
        ensureNotBlocked();

        if (publishedVersion == null) {
            throw new BusinessException(CourseErrorCode.COURSE_HAS_NO_PUBLISHED_VERSION);
        }

        this.status = ContentStatus.ARCHIVED;
    }

    public void unarchive() {
        ensureNotDeleted();
        ensureNotBlocked();

        if (publishedVersion == null) {
            throw new BusinessException(CourseErrorCode.COURSE_HAS_NO_PUBLISHED_VERSION);
        }

        this.status = ContentStatus.ACTIVE;
    }

    public void delete() {
        ensureNotBlocked();

        this.status = ContentStatus.DELETED;
    }

    public void block() {
        ensureNotDeleted();

        this.status = ContentStatus.BLOCKED;
    }

    public boolean isActive() {
        return status == ContentStatus.ACTIVE
                && publishedVersion != null;
    }

    public boolean isEditable() {
        return ContentStatus.BLOCKED != this.status
                && ContentStatus.DELETED != this.status
                && draftVersion != null
                && draftVersion.isEditable();
    }

    public boolean isLessonOpened(UUID stableLessonId,
                                  Instant now) {
        if (stableLessonId == null) {
            return true;
        }

        if (lessonOpenings == null || lessonOpenings.isEmpty()) {
            return true;
        }

        return lessonOpenings.stream()
                .filter(opening ->
                        stableLessonId.equals(opening.getStableLessonId())
                )
                .findFirst()
                .map(CourseLessonOpening::getOpensAt)
                .map(opensAt -> !opensAt.isAfter(now))
                .orElse(true);
    }

    private void ensureCanEditDraft() {
        ensureNotDeleted();
        ensureNotBlocked();

        if (draftVersion == null) {
            throw new BusinessException(CourseErrorCode.COURSE_DRAFT_VERSION_NOT_FOUND);
        }

        if (!draftVersion.isEditable()) {
            throw new BusinessException(CourseErrorCode.COURSE_VERSION_NOT_EDITABLE);
        }
    }

    private void ensureNotDeleted() {
        if (ContentStatus.DELETED == this.status) {
            throw new BusinessException(CourseErrorCode.COURSE_DELETED);
        }
    }

    private void ensureNotBlocked() {
        if (ContentStatus.BLOCKED == this.status) {
            throw new BusinessException(CourseErrorCode.COURSE_BLOCKED);
        }
    }

    public void updateLessonOpensAt(UUID stableLessonId,
                                    Instant opensAt) {
        ensureNotDeleted();
        ensureNotBlocked();

        if (stableLessonId == null) {
            throw new BusinessException(CourseErrorCode.STABLE_LESSON_ID_REQUIRED);
        }

        lessonOpenings.removeIf(opening ->
                stableLessonId.equals(opening.getStableLessonId())
        );

        lessonOpenings.add(
                CourseLessonOpening.create(stableLessonId, opensAt)
        );
    }
}