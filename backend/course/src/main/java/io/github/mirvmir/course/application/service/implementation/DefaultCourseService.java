package io.github.mirvmir.course.application.service.implementation;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.common.exception.UnauthorizedException;
import io.github.mirvmir.course.application.service.mapper.CourseResponseMapper;
import io.github.mirvmir.course.application.service.port.repository.CourseRepository;
import io.github.mirvmir.course.application.service.interfaces.CourseService;
import io.github.mirvmir.course.domain.Course;
import io.github.mirvmir.course.exception.CourseErrorCode;
import io.github.mirvmir.course.web.response.CourseInfoResponse;
import io.github.mirvmir.enrollment.api.EnrollmentApi;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.profile.api.ProfileApi;
import io.github.mirvmir.profile.api.dto.ProfileNameDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Slf4j
@Service
public class DefaultCourseService implements CourseService {

    private final IdentityApi identityApi;
    private final EnrollmentApi enrollmentApi;
    private final ProfileApi profileApi;

    private final CourseRepository courseRepository;

    private final CourseResponseMapper courseResponseMapper;

    @Override
    @Transactional(readOnly = true)
    public CourseInfoResponse getCourse(Long courseId) {
        log.info("Getting course info: courseId={}", courseId);

        Course course = courseRepository.findActiveById(courseId);

        if (course == null) {
            log.error("Course info getting failed, course not found: courseId={}", courseId);
            throw new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND,
                    "Course with id=" + courseId + " not found");
        }

        Long authorId = course.getAuthorId();
        ProfileNameDto author = profileApi.getProfileName(authorId);

        Long currentUserId = identityApi.getCurrentUserId();

        boolean isStudent = false;
        if (currentUserId != null) {
            isStudent = enrollmentApi.isStudentOfCourse(courseId, currentUserId);
        }

        CourseInfoResponse response = courseResponseMapper.toCourseInfoResponse(
                course,
                course.getPublishedVersion(),
                author,
                isStudent
        );

        log.info("Course info successfully received: courseId={}, currentUserId={}, isStudent={}",
                courseId, currentUserId, isStudent);

        return response;
    }
}
