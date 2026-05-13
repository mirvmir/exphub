package io.github.mirvmir.course.application.service;

import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.course.api.CourseApi;
import io.github.mirvmir.course.api.dto.CourseLessonInfoResponse;
import io.github.mirvmir.course.api.dto.CoursePurchaseInfoResponse;
import io.github.mirvmir.course.api.dto.CourseTeacherResponse;
import io.github.mirvmir.course.application.service.mapper.CourseResponseMapper;
import io.github.mirvmir.course.application.service.port.repository.CourseRepository;
import io.github.mirvmir.course.application.service.port.repository.CourseLessonRepository;
import io.github.mirvmir.course.domain.*;
import io.github.mirvmir.course.exception.CourseErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@AllArgsConstructor
@Service
public class DefaultCourseApi implements CourseApi {

    private final CourseRepository courseRepository;
    private final CourseLessonRepository courseLessonRepository;

    private final CourseResponseMapper courseResponseMapper;

    @Override
    @Transactional(readOnly = true)
    public CoursePurchaseInfoResponse getInfo(Long courseId) {
        Course course = courseRepository.findById(courseId);

        return courseResponseMapper.toCoursePurchaseInfoResponse(
                course,
                course.getPublishedVersion()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CourseLessonInfoResponse getLessonInfo(Long courseLessonId) {
        return courseLessonRepository.getLessonInfo(courseLessonId);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseTeacherResponse getCourseTeacher(Long courseId) {
        Course course = courseRepository.findById(courseId);

        if (course == null) {
            throw new NotFoundException(CourseErrorCode.COURSE_NOT_FOUND);
        }

        return new CourseTeacherResponse(
                course.getId(),
                course.getAuthorId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canTeacherAccessFile(Long userId,
                                        Long fileId) {
        return courseRepository.canTeacherAccessFile(userId, fileId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canTeacherAccessVideo(Long userId, Long videoId) {
        return courseRepository.canTeacherAccessVideo(userId, videoId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isPractice(Long courseLessonId) {
        return courseRepository.isPractice(courseLessonId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> findCourseIdsByFileId(Long fileId) {
        return courseRepository.findCourseIdsByFileId(fileId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> findCourseIdsByVideoId(Long videoId) {
        return courseRepository.findCourseIdsByVideoId(videoId);
    }
}
