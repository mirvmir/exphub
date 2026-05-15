package io.github.mirvmir.course.application.service.implementation;

import io.github.mirvmir.common.exception.BusinessException;
import io.github.mirvmir.common.exception.ForbiddenException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.course.application.service.mapper.CourseResponseMapper;
import io.github.mirvmir.course.application.service.port.repository.CourseRepository;
import io.github.mirvmir.course.application.service.port.repository.CourseVersionRepository;
import io.github.mirvmir.course.application.service.interfaces.StudentCourseService;
import io.github.mirvmir.course.domain.Course;
import io.github.mirvmir.course.domain.CourseLesson;
import io.github.mirvmir.course.domain.CourseModule;
import io.github.mirvmir.course.domain.CourseVersion;
import io.github.mirvmir.course.exception.CourseErrorCode;
import io.github.mirvmir.course.web.response.CourseInfoResponse;
import io.github.mirvmir.course.web.response.StudentCourseLessonResponse;
import io.github.mirvmir.course.web.response.StudentCourseModuleResponse;
import io.github.mirvmir.course.web.response.StudentCourseResponse;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import io.github.mirvmir.enrollment.api.dto.StudentCourseEnrollmentResponse;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.profile.api.ProfileApi;
import io.github.mirvmir.profile.api.dto.ProfileNameDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Slf4j
@Service
public class DefaultStudentCourseService implements StudentCourseService {

    private final IdentityApi identityApi;
    private final EnrollmentApi enrollmentApi;
    private final ProfileApi profileApi;

    private final CourseRepository courseRepository;
    private final CourseVersionRepository courseVersionRepository;

    private final CourseResponseMapper courseResponseMapper;

    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public CourseInfoResponse getCourseDescription(Long courseId) {
        log.info("Getting student course description: courseId={}", courseId);

        StudentCourseEnrollmentResponse enrollment =
                getCurrentStudentEnrollment(courseId);

        Course course = getExistingCourseWithSettings(courseId);

        CourseVersion version =
                courseVersionRepository.findById(enrollment.courseVersionId());

        if (version == null) {
            log.error("Student course description getting failed, course version not found: courseId={}, courseVersionId={}",
                    courseId, enrollment.courseVersionId());
            throw new NotFoundException(
                    CourseErrorCode.COURSE_VERSION_NOT_FOUND,
                    "Course version with id=" + enrollment.courseVersionId() + " not found"
            );
        }

        ProfileNameDto author =
                profileApi.getProfileName(course.getAuthorId());

        CourseInfoResponse response = courseResponseMapper.toCourseInfoResponse(
                course,
                version,
                author,
                true
        );

        log.info("Student course description successfully received: courseId={}, courseVersionId={}",
                courseId, enrollment.courseVersionId());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public StudentCourseResponse getCourse(Long courseId) {
        log.info("Getting student course: courseId={}", courseId);

        StudentCourseEnrollmentResponse enrollment =
                getCurrentStudentEnrollment(courseId);

        Course course = getExistingCourseWithSettings(courseId);

        CourseVersion version =
                courseVersionRepository.findByIdWithModules(enrollment.courseVersionId());

        if (version == null) {
            log.error("Student course getting failed, course version not found: courseId={}, courseVersionId={}",
                    courseId, enrollment.courseVersionId());
            throw new NotFoundException(
                    CourseErrorCode.COURSE_VERSION_NOT_FOUND,
                    "Course version with id=" + enrollment.courseVersionId() + " not found"
            );
        }

        StudentCourseResponse response = courseResponseMapper.toStudentCourseResponse(
                course,
                version,
                enrollment
        );

        log.info("Student course successfully received: courseId={}, courseVersionId={}",
                courseId, enrollment.courseVersionId());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public StudentCourseModuleResponse getModule(Long courseId,
                                                 UUID stableModuleId) {
        log.info("Getting student course module: courseId={}, stableModuleId={}",
                courseId, stableModuleId);

        StudentCourseEnrollmentResponse enrollment =
                getCurrentStudentEnrollment(courseId);

        Course course = getExistingCourseWithSettings(courseId);

        CourseVersion version =
                courseVersionRepository.findByIdWithModule(
                        enrollment.courseVersionId(),
                        stableModuleId
                );

        if (version == null) {
            log.error("Student course module getting failed, module not found: courseId={}, courseVersionId={}, stableModuleId={}",
                    courseId, enrollment.courseVersionId(), stableModuleId);
            throw new NotFoundException(
                    CourseErrorCode.COURSE_MODULE_NOT_FOUND,
                    "Course module with stableModuleId=" + stableModuleId + " not found"
            );
        }

        CourseModule module =
                version.findModuleByStableId(stableModuleId);

        Instant now = Instant.now(clock);

        StudentCourseModuleResponse response = courseResponseMapper.toStudentCourseModuleResponse(
                course,
                module,
                now
        );

        log.info("Student course module successfully received: courseId={}, courseVersionId={}, stableModuleId={}",
                courseId, enrollment.courseVersionId(), stableModuleId);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public StudentCourseLessonResponse getLesson(Long courseId,
                                                 UUID stableLessonId) {
        log.info("Getting student course lesson: courseId={}, stableLessonId={}",
                courseId, stableLessonId);

        StudentCourseEnrollmentResponse enrollment =
                getCurrentStudentEnrollment(courseId);

        Course course = getExistingCourseWithSettings(courseId);

        CourseVersion version =
                courseVersionRepository.findByIdWithLesson(
                        enrollment.courseVersionId(),
                        stableLessonId
                );

        if (version == null) {
            log.error("Student course lesson getting failed, lesson not found: courseId={}, courseVersionId={}, stableLessonId={}",
                    courseId, enrollment.courseVersionId(), stableLessonId);
            throw new NotFoundException(
                    CourseErrorCode.COURSE_LESSON_NOT_FOUND,
                    "Course lesson with stableLessonId=" + stableLessonId + " not found"
            );
        }

        Instant now = Instant.now(clock);

        if (!course.isLessonOpened(stableLessonId, now)) {
            log.error("Student course lesson getting failed, lesson is not opened yet: courseId={}, stableLessonId={}, now={}",
                    courseId, stableLessonId, now);
            throw new ForbiddenException(
                    CourseErrorCode.COURSE_LESSON_NOT_OPENED,
                    "Lesson with stableLessonId=" + stableLessonId + " is not opened yet"
            );
        }

        CourseLesson lesson =
                version.findLessonByStableId(stableLessonId);

        StudentCourseLessonResponse response =
                courseResponseMapper.toStudentCourseLessonResponse(lesson);

        log.info("Student course lesson successfully received: courseId={}, courseVersionId={}, stableLessonId={}",
                courseId, enrollment.courseVersionId(), stableLessonId);

        return response;
    }

    private StudentCourseEnrollmentResponse getCurrentStudentEnrollment(Long courseId) {
        Long studentId = identityApi.getCurrentUserId();

        StudentCourseEnrollmentResponse enrollment =
                enrollmentApi.getStudentCourseEnrollment(
                        studentId,
                        courseId
                );

        if (enrollment == null) {
            log.error("Student enrollment getting failed, enrollment not found: studentId={}, courseId={}",
                    studentId, courseId);
            throw new NotFoundException(
                    CourseErrorCode.COURSE_ENROLLMENT_NOT_FOUND,
                    "Enrollment for courseId=" + courseId + " not found"
            );
        }

        if (enrollment.courseVersionId() == null) {
            log.error("Student enrollment getting failed, course version is not assigned: studentId={}, courseId={}",
                    studentId, courseId);
            throw new BusinessException(
                    CourseErrorCode.COURSE_ENROLLMENT_VERSION_NOT_FOUND
            );
        }

        return enrollment;
    }

    private Course getExistingCourseWithSettings(Long courseId) {
        Course course =
                courseRepository.findByIdWithSettings(courseId);

        if (course == null || !course.isActive()) {
            log.error("Active course with settings getting failed: courseId={}", courseId);
            throw new NotFoundException(
                    CourseErrorCode.COURSE_NOT_FOUND,
                    "Course with id=" + courseId + " not found"
            );
        }

        return course;
    }
}