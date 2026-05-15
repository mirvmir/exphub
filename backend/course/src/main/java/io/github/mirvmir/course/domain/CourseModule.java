package io.github.mirvmir.course.domain;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.course.exception.CourseErrorCode;
import io.github.mirvmir.course.web.request.SaveDraftLessonItemRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class CourseModule {
    private Long id;
    private UUID stableModuleId;
    private String title;
    private Integer sortOrder;
    private List<CourseLesson> lessons;

    public static CourseModule create(String title) {
        return new CourseModule(
                null,
                UUID.randomUUID(),
                title,
                null,
                new ArrayList<>()
        );
    }

    public static CourseModule copyToDraftFromPublished(CourseModule source) {
        return new CourseModule(
                null,
                source.getStableModuleId(),
                source.getTitle(),
                source.getSortOrder(),
                source.getLessons()
                        .stream()
                        .sorted(Comparator.comparing(
                                CourseLesson::getSortOrder,
                                Comparator.nullsLast(Integer::compareTo)
                        ))
                        .map(CourseLesson::copyToDraftFromPublished)
                        .collect(Collectors.toCollection(ArrayList::new))
        );
    }

    public static CourseModule load(Long id,
                                    UUID stableModuleId,
                                    String title,
                                    Integer sortOrder,
                                    List<CourseLesson> lessons) {
        return new CourseModule(
                id,
                stableModuleId,
                title,
                sortOrder,
                lessons == null ? new ArrayList<>() : new ArrayList<>(lessons)
        );
    }

    public void update(String title) {
        this.title = title;
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void saveLessons(List<SaveDraftLessonItemRequest> lessonRequests) {
        if (lessonRequests == null) {
            this.lessons.clear();
            return;
        }

        Map<Long, CourseLesson> existingById = lessons.stream()
                .filter(lesson -> lesson.getId() != null)
                .collect(Collectors.toMap(
                        CourseLesson::getId,
                        Function.identity()
                ));

        List<CourseLesson> result = new ArrayList<>();

        for (int i = 0; i < lessonRequests.size(); i++) {
            SaveDraftLessonItemRequest request = lessonRequests.get(i);

            CourseLesson lesson;

            if (request.id() == null) {
                lesson = CourseLesson.create(
                        request.title(),
                        request.type()
                );
            } else {
                lesson = existingById.get(request.id());

                if (lesson == null) {
                    throw new NotFoundException(
                            CourseErrorCode.COURSE_LESSON_NOT_FOUND,
                            "Course lesson with id=" + request.id() + " not found in draft module"
                    );
                }

                lesson.update(
                        request.title(),
                        request.type()
                );
            }

            lesson.updateSortOrder(resolveSortOrder(request.sortOrder(), i));

            result.add(lesson);
        }

        this.lessons.clear();
        this.lessons.addAll(result);
    }

    public void validateBeforePublication() {
        if (title == null || title.isBlank()) {
            throw new BusinessException(CourseErrorCode.COURSE_MODULE_TITLE_REQUIRED);
        }

        if (lessons == null || lessons.isEmpty()) {
            throw new BusinessException(CourseErrorCode.COURSE_MODULE_LESSONS_REQUIRED);
        }

        for (CourseLesson lesson : lessons) {
            lesson.validateBeforePublication();
        }
    }

    public boolean isPublishable() {
        return !(title == null || title.isBlank())
                && !(lessons == null || lessons.isEmpty())
                && lessons.stream().allMatch(lesson -> lesson.isPublishable());
    }

    public List<CourseLesson> getLessons() {
        if (lessons == null) {
            return new ArrayList<>();
        }

        return lessons;
    }

    private Integer resolveSortOrder(Integer requestSortOrder,
                                     int index) {
        return requestSortOrder == null
                ? index
                : requestSortOrder;
    }
}