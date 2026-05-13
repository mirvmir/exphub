package io.github.mirvmir.course.api;

import io.github.mirvmir.course.api.dto.CourseLessonInfoResponse;
import io.github.mirvmir.course.api.dto.CoursePurchaseInfoResponse;
import io.github.mirvmir.course.api.dto.CourseTeacherResponse;

import java.util.Set;

public interface CourseApi {
    CoursePurchaseInfoResponse getInfo(Long courseId);
    CourseLessonInfoResponse getLessonInfo(Long courseLessonId);
    CourseTeacherResponse getCourseTeacher(Long courseId);
    boolean canTeacherAccessFile(Long userId, Long fileId);
    boolean canTeacherAccessVideo(Long userId, Long videoId);
    boolean isPractice(Long lessonId);
    Set<Long> findCourseIdsByFileId(Long fileId);
    Set<Long> findCourseIdsByVideoId(Long videoId);
}
