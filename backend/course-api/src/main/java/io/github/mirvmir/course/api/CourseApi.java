package io.github.mirvmir.course.api;

import io.github.mirvmir.course.api.dto.CourseLessonInfoResponse;
import io.github.mirvmir.course.api.dto.CourseBookingInfoResponse;
import io.github.mirvmir.course.api.dto.CourseTeacherResponse;

import java.util.Set;
import java.util.UUID;

public interface CourseApi {
    CourseBookingInfoResponse getInfo(Long courseId);
    CourseLessonInfoResponse getLessonInfo(Long courseLessonId);
    CourseTeacherResponse getCourseTeacher(Long courseId);
    Long getCourseIdByStableLessonId(UUID stableLessonId);
    boolean canTeacherAccessFile(Long userId, Long fileId);
    boolean canTeacherAccessVideo(Long userId, Long videoId);
    boolean isPractice(UUID stableLessonId);
    Set<Long> findCourseIdsByFileId(Long fileId);
    Set<Long> findCourseIdsByVideoId(Long videoId);
}
