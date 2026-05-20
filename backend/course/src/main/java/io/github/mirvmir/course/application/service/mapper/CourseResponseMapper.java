package io.github.mirvmir.course.application.service.mapper;

import io.github.mirvmir.course.api.dto.CourseBookingInfoResponse;
import io.github.mirvmir.course.domain.Course;
import io.github.mirvmir.course.domain.CourseLesson;
import io.github.mirvmir.course.domain.CourseModule;
import io.github.mirvmir.course.domain.CourseVersion;
import io.github.mirvmir.course.domain.LessonBlock;
import io.github.mirvmir.course.domain.CourseLessonOpening;
import io.github.mirvmir.course.web.response.*;
import io.github.mirvmir.enrollment.api.dto.StudentCourseEnrollmentResponse;
import io.github.mirvmir.profile.api.dto.ProfileNameDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Service
public class CourseResponseMapper {

    public AuthorCourseResponse toAuthorCourseResponse(
            Course course,
            CourseVersion draftVersion,
            ProfileNameDto author,
            boolean canEdit,
            boolean canPublication
    ) {
        List<AuthorCourseModuleShortResponse> modules =
                draftVersion.getModules()
                        .stream()
                        .sorted(Comparator.comparing(
                                CourseModule::getSortOrder,
                                Comparator.nullsLast(Integer::compareTo)
                        ))
                        .map(this::toAuthorCourseModuleShortResponse)
                        .toList();

        return new AuthorCourseResponse(
                course.getId(),
                draftVersion.getId(),
                course.getAuthorId(),
                author,
                draftVersion.getTitle(),
                draftVersion.getShortDescription(),
                draftVersion.getDescriptionHtml(),
                draftVersion.getPrice() == null
                        ? null
                        : draftVersion.getPrice().getAmount(),
                draftVersion.getPrice() == null
                        ? null
                        : draftVersion.getPrice().getCurrency(),
                course.getStatus(),
                course.getTopicIds(),
                canEdit,
                canPublication,
                modules
        );
    }

    public AuthorCourseModuleResponse toAuthorCourseModuleResponse(
            CourseModule module,
            boolean canEdit
    ) {
        List<AuthorCourseLessonShortResponse> lessons =
                module.getLessons()
                        .stream()
                        .sorted(Comparator.comparing(
                                CourseLesson::getSortOrder,
                                Comparator.nullsLast(Integer::compareTo)
                        ))
                        .map(this::toAuthorCourseLessonShortResponse)
                        .toList();

        return new AuthorCourseModuleResponse(
                module.getId(),
                module.getStableModuleId(),
                module.getTitle(),
                module.getSortOrder(),
                canEdit,
                lessons
        );
    }

    public AuthorCourseLessonResponse toAuthorCourseLessonResponse(
            CourseLesson lesson,
            boolean canEdit
    ) {
        List<AuthorLessonBlockResponse> blocks =
                lesson.getBlocks()
                        .stream()
                        .sorted(Comparator.comparing(
                                LessonBlock::getSortOrder,
                                Comparator.nullsLast(Integer::compareTo)
                        ))
                        .map(this::toAuthorLessonBlockResponse)
                        .toList();

        return new AuthorCourseLessonResponse(
                lesson.getId(),
                lesson.getStableLessonId(),
                lesson.getTitle(),
                lesson.getType(),
                lesson.getSortOrder(),
                canEdit,
                blocks
        );
    }

    private AuthorCourseModuleShortResponse toAuthorCourseModuleShortResponse(
            CourseModule module
    ) {
        return new AuthorCourseModuleShortResponse(
                module.getId(),
                module.getStableModuleId(),
                module.getTitle(),
                module.getSortOrder()
        );
    }

    private AuthorCourseLessonShortResponse toAuthorCourseLessonShortResponse(
            CourseLesson lesson
    ) {
        return new AuthorCourseLessonShortResponse(
                lesson.getId(),
                lesson.getStableLessonId(),
                lesson.getTitle(),
                lesson.getType(),
                lesson.getSortOrder()
        );
    }

    private AuthorLessonBlockResponse toAuthorLessonBlockResponse(
            LessonBlock block
    ) {
        return new AuthorLessonBlockResponse(
                block.getId(),
                block.getStableBlockId(),
                block.getType(),
                block.getSortOrder(),
                block.getHtml(),
                block.getFileAssetId(),
                block.getVideoAssetId()
        );
    }

    public CourseInfoResponse toCourseInfoResponse(
            Course course,
            CourseVersion version,
            ProfileNameDto author,
            boolean isStudent
    ) {
        return new CourseInfoResponse(
                course.getId(),
                course.getAuthorId(),
                author,
                version.getTitle(),
                version.getShortDescription(),
                version.getDescriptionHtml(),
                version.getPrice().getAmount(),
                version.getPrice().getCurrency(),
                course.getTopicIds(),
                isStudent
        );
    }

    public CourseBookingInfoResponse toCourseBookingInfoResponse(
            Course course,
            CourseVersion version
    ) {
        return new CourseBookingInfoResponse(
                course.getId(),
                version.getId(),
                course.getAuthorId(),
                version.getTitle(),
                version.getPrice().getAmount(),
                version.getPrice().getCurrency(),
                course.isActive()
        );
    }

    public StudentCourseResponse toStudentCourseResponse(
            Course course,
            CourseVersion version,
            StudentCourseEnrollmentResponse enrollment
    ) {
        List<StudentCourseModuleShortResponse> modules =
                version.getModules()
                        .stream()
                        .sorted(Comparator.comparing(
                                CourseModule::getSortOrder,
                                Comparator.nullsLast(Integer::compareTo)
                        ))
                        .map(this::toStudentCourseModuleShortResponse)
                        .toList();

        return new StudentCourseResponse(
                course.getId(),
                version.getId(),
                enrollment.enrollmentId(),
                version.getTitle(),
                modules
        );
    }

    public StudentCourseModuleResponse toStudentCourseModuleResponse(Course course,
                                                                     CourseModule module,
                                                                     Instant now) {
        List<StudentCourseLessonShortResponse> lessons =
                module.getLessons()
                        .stream()
                        .sorted(Comparator.comparing(
                                CourseLesson::getSortOrder,
                                Comparator.nullsLast(Integer::compareTo)
                        ))
                        .map(lesson -> toStudentCourseLessonShortResponse(
                                course,
                                lesson,
                                now
                        ))
                        .toList();

        return new StudentCourseModuleResponse(
                module.getStableModuleId(),
                module.getTitle(),
                module.getSortOrder(),
                lessons
        );
    }

    public StudentCourseLessonResponse toStudentCourseLessonResponse(CourseLesson lesson) {
        List<StudentLessonBlockResponse> blocks =
                lesson.getBlocks()
                        .stream()
                        .sorted(Comparator.comparing(
                                LessonBlock::getSortOrder,
                                Comparator.nullsLast(Integer::compareTo)
                        ))
                        .map(this::toStudentLessonBlockResponse)
                        .toList();

        return new StudentCourseLessonResponse(
                lesson.getStableLessonId(),
                lesson.getTitle(),
                lesson.getType(),
                lesson.getSortOrder(),
                blocks
        );
    }

    private StudentCourseModuleShortResponse toStudentCourseModuleShortResponse(
            CourseModule module
    ) {
        return new StudentCourseModuleShortResponse(
                module.getStableModuleId(),
                module.getTitle(),
                module.getSortOrder()
        );
    }

    private StudentCourseLessonShortResponse toStudentCourseLessonShortResponse(
            Course course,
            CourseLesson lesson,
            Instant now
    ) {
        Instant opensAt = findLessonOpensAt(
                course,
                lesson.getStableLessonId()
        );

        boolean opened = isLessonOpened(
                opensAt,
                now
        );

        return new StudentCourseLessonShortResponse(
                lesson.getStableLessonId(),
                lesson.getTitle(),
                lesson.getType(),
                lesson.getSortOrder(),
                opensAt,
                opened
        );
    }

    private StudentLessonBlockResponse toStudentLessonBlockResponse(
            LessonBlock block
    ) {
        return new StudentLessonBlockResponse(
                block.getId(),
                block.getStableBlockId(),
                block.getType(),
                block.getSortOrder(),
                block.getHtml(),
                block.getFileAssetId(),
                block.getVideoAssetId()
        );
    }

    private Instant findLessonOpensAt(
            Course course,
            UUID stableLessonId
    ) {
        if (course.getLessonOpenings() == null || stableLessonId == null) {
            return null;
        }

        Optional<CourseLessonOpening> opening =
                course.getLessonOpenings()
                        .stream()
                        .filter(item ->
                                stableLessonId.equals(item.getStableLessonId())
                        )
                        .findFirst();

        return opening
                .map(CourseLessonOpening::getOpensAt)
                .orElse(null);
    }

    private boolean isLessonOpened(
            Instant opensAt,
            Instant now
    ) {
        if (opensAt == null) {
            return true;
        }

        return !opensAt.isAfter(now);
    }
}