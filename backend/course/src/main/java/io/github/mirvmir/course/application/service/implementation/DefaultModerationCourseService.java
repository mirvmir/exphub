package io.github.mirvmir.course.application.service.implementation;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.course.api.event.CourseDeleteEvent;
import io.github.mirvmir.course.api.event.CoursePublishedEvent;
import io.github.mirvmir.course.application.service.port.event.CourseEventPublisher;
import io.github.mirvmir.course.application.service.port.repository.CourseRepository;
import io.github.mirvmir.course.application.service.interfaces.ModerationCourseService;
import io.github.mirvmir.course.application.service.port.repository.CourseVersionRepository;
import io.github.mirvmir.course.domain.Course;
import io.github.mirvmir.course.exception.CourseErrorCode;
import io.github.mirvmir.course.web.request.RejectCourseRequest;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Slf4j
@Service
public class DefaultModerationCourseService implements ModerationCourseService {

    private final CourseRepository courseRepository;
    private final CourseVersionRepository courseVersionRepository;
    private final EnrollmentApi enrollmentApi;
    private final CourseEventPublisher eventPublisher;

    @Override
    @Transactional
    public void approve(Long courseId) {
        log.info("Course moderation approval requested: courseId={}", courseId);

        Course course = getExistingCourseWithDraft(courseId);

        boolean wasActive = course.isActive();

        course.approveModeration();

        courseRepository.approveDraft(course);

        if (wasActive) {
            eventPublisher.delete(new CourseDeleteEvent(course.getId()));
        }

        eventPublisher.publish(
                new CoursePublishedEvent(
                        course.getId(),
                        course.getAuthorId(),
                        course.getPublishedVersion().getTitle(),
                        course.getPublishedVersion().getShortDescription(),
                        course.getPublishedVersion().getPrice().getAmount(),
                        course.getPublishedVersion().getPrice().getCurrency(),
                        course.getTopicIds()
                )
        );

        log.info("Course moderation approval completed successfully: courseId={}", courseId);
    }

    @Override
    @Transactional
    public void reject(Long courseId,
                       RejectCourseRequest request) {
        log.info("Course moderation rejection requested: courseId={}", courseId);

        Course course = getExistingCourseWithDraft(courseId);

        course.rejectModeration(request.moderationComment());

        courseVersionRepository.updateModerationState(course.getDraftVersion());

        log.info("Course moderation rejection completed successfully: courseId={}", courseId);
    }

    @Override
    @Transactional
    public void block(Long courseId) {
        log.info("Course blocking requested: courseId={}", courseId);

        Course course = courseRepository.findByIdWithPublishedInfo(courseId);

        if (course == null) {
            log.error("Course blocking failed, course not found: courseId={}", courseId);
            throw new NotFoundException(
                    CourseErrorCode.COURSE_NOT_FOUND,
                    "Course with id=" + courseId + " not found"
            );
        }

        course.block();

        courseRepository.updateStatus(course);

        enrollmentApi.refundPayedCourseEnrollments(
                courseId,
                "Курс заблокирован"
        );

        eventPublisher.delete(
                new CourseDeleteEvent(course.getId())
        );

        log.info("Course blocking completed successfully: courseId={}", courseId);
    }

    private Course getExistingCourseWithDraft(Long courseId) {
        Course course = courseRepository.findByIdWithDraftContent(courseId);

        if (course == null) {
            log.error("Course loading with draft failed, course not found: courseId={}", courseId);
            throw new NotFoundException(
                    CourseErrorCode.COURSE_NOT_FOUND,
                    "Course with id=" + courseId + " not found"
            );
        }

        return course;
    }
}
