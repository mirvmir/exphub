package io.github.mirvmir.course.domain;

import io.github.mirvmir.common.domain.ModerationStatus;
import io.github.mirvmir.common.domain.Money;
import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.course.exception.CourseErrorCode;
import io.github.mirvmir.course.web.request.SaveDraftLessonBlockItemRequest;
import io.github.mirvmir.course.web.request.SaveDraftLessonItemRequest;
import io.github.mirvmir.course.web.request.SaveDraftModuleItemRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class CourseVersion {
    private Long id;
    private ModerationStatus status;
    private String title;
    private String shortDescription;
    private String descriptionHtml;
    private Money price;
    private String moderationComment;
    private List<CourseModule> modules;

    public static CourseVersion createDraft(String title) {
        return new CourseVersion(
                null,
                ModerationStatus.DRAFT,
                title,
                null,
                null,
                null,
                null,
                new ArrayList<>()
        );
    }

    public static CourseVersion copyToDraftFromPublished(CourseVersion source) {
        return new CourseVersion(
                null,
                ModerationStatus.DRAFT,
                source.getTitle(),
                source.getShortDescription(),
                source.getDescriptionHtml(),
                source.getPrice(),
                null,
                source.getModules()
                        .stream()
                        .sorted(Comparator.comparing(
                                CourseModule::getSortOrder,
                                Comparator.nullsLast(Integer::compareTo)
                        ))
                        .map(CourseModule::copyToDraftFromPublished)
                        .collect(Collectors.toCollection(ArrayList::new))
        );
    }

    public static CourseVersion load(
            Long id,
            ModerationStatus status,
            String title,
            String shortDescription,
            String descriptionHtml,
            BigDecimal priceAmount,
            Currency priceCurrency,
            String moderationComment,
            List<CourseModule> modules
    ) {
        Money price = new Money(priceAmount, priceCurrency);
        return new CourseVersion(
                id,
                status,
                title,
                shortDescription,
                HtmlSanitizer.sanitize(descriptionHtml),
                price,
                moderationComment,
                modules == null
                        ? new ArrayList<>()
                        : new ArrayList<>(modules)
        );
    }

    public boolean isEditable() {
        return ModerationStatus.DRAFT == this.status
                || ModerationStatus.REJECTED == this.status;
    }

    public void updateCourseInfo(
            String title,
            String shortDescription,
            String descriptionHtml,
            BigDecimal priceAmount,
            Currency priceCurrency
    ) {
        ensureEditable();

        this.title = title;
        this.shortDescription = shortDescription;
        this.descriptionHtml = HtmlSanitizer.sanitize(descriptionHtml);
        this.price = new Money(priceAmount, priceCurrency);
    }

    public void saveModules(List<SaveDraftModuleItemRequest> moduleRequests) {
        ensureEditable();

        if (moduleRequests == null) {
            this.modules.clear();
            return;
        }

        Map<Long, CourseModule> existingById = modules.stream()
                .filter(module -> module.getId() != null)
                .collect(Collectors.toMap(
                        CourseModule::getId,
                        Function.identity()
                ));

        List<CourseModule> result = new ArrayList<>();

        for (int i = 0; i < moduleRequests.size(); i++) {
            SaveDraftModuleItemRequest request = moduleRequests.get(i);

            CourseModule module;

            if (request.id() == null) {
                module = CourseModule.create(
                        request.title()
                );
            } else {
                module = existingById.get(request.id());

                if (module == null) {
                    throw new NotFoundException(
                            CourseErrorCode.COURSE_MODULE_NOT_FOUND,
                            "Course module with id=" + request.id() + " not found in draft version"
                    );
                }

                module.update(
                        request.title()
                );
            }

            module.updateSortOrder(resolveSortOrder(request.sortOrder(), i));

            result.add(module);
        }

        this.modules.clear();
        this.modules.addAll(result);
    }

    public void saveModuleLessons(Long moduleId,
                                  List<SaveDraftLessonItemRequest> lessonRequests) {
        ensureEditable();

        CourseModule module = findModule(moduleId);

        module.saveLessons(lessonRequests);
    }

    public void saveLessonBlocks(Long lessonId,
                                 List<SaveDraftLessonBlockItemRequest> blockRequests) {
        ensureEditable();

        CourseLesson lesson = findLesson(lessonId);

        lesson.saveBlocks(blockRequests);
    }

    public void sendToModeration() {
        ensureEditable();

        this.status = ModerationStatus.PENDING;
        this.moderationComment = null;
    }

    public void approveModeration() {
        if (ModerationStatus.PENDING != this.status) {
            throw new BusinessException(CourseErrorCode.COURSE_VERSION_NOT_ON_MODERATION);
        }

        this.status = ModerationStatus.APPROVED;
        this.moderationComment = null;
    }

    public void rejectModeration(String moderationComment) {
        if (ModerationStatus.PENDING != this.status) {
            throw new BusinessException(CourseErrorCode.COURSE_VERSION_NOT_ON_MODERATION);
        }

        if (moderationComment == null || moderationComment.isBlank()) {
            throw new BusinessException(CourseErrorCode.MODERATION_COMMENT_REQUIRED);
        }

        this.status = ModerationStatus.REJECTED;
        this.moderationComment = moderationComment;
    }

    public void validateBeforePublication() {
        if (title == null || title.isBlank()) {
            throw new BusinessException(CourseErrorCode.COURSE_TITLE_REQUIRED);
        }

        if (descriptionHtml == null || descriptionHtml.isBlank()) {
            throw new BusinessException(CourseErrorCode.COURSE_DESCRIPTION_REQUIRED);
        }

        if (modules == null || modules.isEmpty()) {
            throw new BusinessException(CourseErrorCode.COURSE_MODULES_REQUIRED);
        }

        for (CourseModule module : modules) {
            module.validateBeforePublication();
        }
    }

    public boolean isPublishable() {
        return !(title == null || title.isBlank())
                && !(descriptionHtml == null || descriptionHtml.isBlank())
                && !(modules == null || modules.isEmpty())
                && modules.stream().allMatch(module -> module.isPublishable());
    }


    public CourseModule findModule(Long moduleId) {
        return modules.stream()
                .filter(module -> moduleId.equals(module.getId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        CourseErrorCode.COURSE_MODULE_NOT_FOUND,
                        "Course module with id=" + moduleId + " not found in draft version"
                ));
    }

    public CourseLesson findLesson(Long lessonId) {
        return modules.stream()
                .flatMap(module -> module.getLessons().stream())
                .filter(lesson -> lessonId.equals(lesson.getId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        CourseErrorCode.COURSE_LESSON_NOT_FOUND,
                        "Course lesson with id=" + lessonId + " not found in draft version"
                ));
    }

    public CourseModule findModuleByStableId(UUID stableModuleId) {
        if (stableModuleId == null) {
            throw new NotFoundException(
                    CourseErrorCode.COURSE_MODULE_NOT_FOUND,
                    "Course module not found"
            );
        }

        return getModules()
                .stream()
                .filter(module ->
                        stableModuleId.equals(module.getStableModuleId())
                )
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        CourseErrorCode.COURSE_MODULE_NOT_FOUND,
                        "Course module with stableModuleId=" + stableModuleId + " not found"
                ));
    }

    public CourseLesson findLessonByStableId(UUID stableLessonId) {
        if (stableLessonId == null) {
            throw new NotFoundException(
                    CourseErrorCode.COURSE_LESSON_NOT_FOUND,
                    "Course lesson not found"
            );
        }

        return getModules()
                .stream()
                .flatMap(module -> module.getLessons().stream())
                .filter(lesson ->
                        stableLessonId.equals(lesson.getStableLessonId())
                )
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        CourseErrorCode.COURSE_LESSON_NOT_FOUND,
                        "Course lesson with stableLessonId=" + stableLessonId + " not found"
                ));
    }

    public List<CourseModule> getModules() {
        if (modules == null) {
            return new ArrayList<>();
        }

        return modules;
    }

    private void ensureEditable() {
        if (!isEditable()) {
            throw new BusinessException(CourseErrorCode.COURSE_VERSION_NOT_EDITABLE);
        }
    }

    private Integer resolveSortOrder(
            Integer requestSortOrder,
            int index
    ) {
        return requestSortOrder == null
                ? index
                : requestSortOrder;
    }
}