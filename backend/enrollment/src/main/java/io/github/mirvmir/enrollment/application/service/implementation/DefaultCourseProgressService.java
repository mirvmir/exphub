package io.github.mirvmir.enrollment.application.service.implementation;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.common.exception.UnauthorizedException;
import io.github.mirvmir.course.api.CourseApi;
import io.github.mirvmir.course.api.dto.CourseLessonInfoResponse;
import io.github.mirvmir.enrollment.application.service.interfaces.CourseProgressService;
import io.github.mirvmir.enrollment.application.service.port.repository.CourseEnrollmentRepository;
import io.github.mirvmir.enrollment.application.service.port.repository.StudentLessonProgressRepository;
import io.github.mirvmir.enrollment.domain.CourseEnrollment;
import io.github.mirvmir.enrollment.domain.StudentLessonProgress;
import io.github.mirvmir.enrollment.exception.EnrollmentErrorCode;
import io.github.mirvmir.enrollment.web.response.CourseProgressResponse;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.practice.api.PracticeApi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.Set;

@AllArgsConstructor
@Slf4j
@Service
public class DefaultCourseProgressService implements CourseProgressService {

    private final IdentityApi identityApi;
    private final CourseApi courseApi;
    private final PracticeApi practiceApi;

    private final CourseEnrollmentRepository courseEnrollmentRepository;
    private final StudentLessonProgressRepository userLessonProgressRepository;

    private final Clock clock;

    @Override
    @Transactional
    public CourseProgressResponse completeLesson(Long courseId,
                                                 Long courseLessonId) {
        Long studentId = identityApi.getCurrentUserId();
        Instant now = Instant.now(clock);

        if (studentId == null) {
            log.error("Unauthorized complete lesson request");
            throw new UnauthorizedException("UNAUTHORIZED", "User not authorized");
        }

        log.debug("Start completing course lesson: studentId={}, courseId={}, courseLessonId={}",
                studentId,
                courseId,
                courseLessonId);

        CourseEnrollment enrollment =
                courseEnrollmentRepository.findPayedByUserIdAndCourseId(
                        studentId,
                        courseId
                );

        if (enrollment == null) {
            log.error("Student tried to complete lesson without paid course enrollment: studentId={}, courseId={}, courseLessonId={}",
                    studentId,
                    courseId,
                    courseLessonId);
            throw new NotFoundException(
                    EnrollmentErrorCode.ENROLLMENT_NOT_FOUND,
                    "Payed enrollment for studentId=" + studentId
                            + " and courseId=" + courseId + " not found"
            );
        }

        CourseLessonInfoResponse lessonInfo =
                courseApi.getLessonInfo(courseLessonId);

        if (lessonInfo == null) {
            log.error("Course lesson info was not found while completing lesson: studentId={}, courseId={}, courseLessonId={}",
                    studentId,
                    courseId,
                    courseLessonId);
            throw new NotFoundException(
                    EnrollmentErrorCode.COURSE_NOT_FOUND,
                    "Course lesson with id=" + courseLessonId + " not found"
            );
        }

        if (lessonInfo.opensAt() != null
                && now.isBefore(lessonInfo.opensAt())) {
            log.error("Course lesson is not opened yet: studentId={}, courseLessonId={}, opensAt={}, now={}",
                    studentId,
                    courseLessonId,
                    lessonInfo.opensAt(),
                    now);
            throw new BusinessException(
                    EnrollmentErrorCode.COURSE_LESSON_NOT_OPENED
            );
        }

        if (lessonInfo.isPractice()) {
            boolean practiceCompleted =
                    practiceApi.isPracticeCompletedByLessonIdAndStudentId(
                            lessonInfo.stableLessonId(),
                            studentId
                    );

            if (!practiceCompleted) {
                log.error("Practice lesson cannot be completed before practice is done: studentId={}, courseLessonId={}",
                        studentId,
                        courseLessonId);
                throw new BusinessException(
                        EnrollmentErrorCode.PRACTICE_NOT_COMPLETED
                );
            }
        }

        Set<Long> moduleLessonIds = lessonInfo.moduleLessonIds() == null
                ? Set.of()
                : lessonInfo.moduleLessonIds();
        Set<Long> courseLessonIds = lessonInfo.courseLessonIds() == null
                ? Set.of()
                : lessonInfo.courseLessonIds();

        boolean alreadyCompleted =
                userLessonProgressRepository.existsByEnrollmentIdAndCourseLessonId(
                        enrollment.getId(),
                        courseLessonId
                );

        if (!alreadyCompleted) {
            StudentLessonProgress progress = StudentLessonProgress.create(
                    enrollment.getId(),
                    courseLessonId,
                    now
            );

            userLessonProgressRepository.saveOrUpdate(progress);
        }

        BigDecimal moduleProgressPercent = calculatePercent(
                userLessonProgressRepository.countCompletedByEnrollmentIdAndLessonIds(
                        enrollment.getId(),
                        moduleLessonIds
                ),
                moduleLessonIds.size()
        );

        BigDecimal courseProgressPercent = calculatePercent(
                userLessonProgressRepository.countCompletedByEnrollmentIdAndLessonIds(
                        enrollment.getId(),
                        courseLessonIds
                ),
                courseLessonIds.size()
        );

        enrollment.updateProgress(courseProgressPercent);

        CourseEnrollment savedEnrollment =
                courseEnrollmentRepository.saveOrUpdate(enrollment);

        return new CourseProgressResponse(
                savedEnrollment.getId(),
                courseId,
                lessonInfo.courseModuleId(),
                courseLessonId,
                BigDecimal.valueOf(100),
                moduleProgressPercent,
                courseProgressPercent,
                savedEnrollment.getStatus().name()
        );
    }

    private BigDecimal calculatePercent(long completed, long total) {
        if (0 == total) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(completed)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }
}
