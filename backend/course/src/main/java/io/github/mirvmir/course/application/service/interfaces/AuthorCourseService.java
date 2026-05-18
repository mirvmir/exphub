package io.github.mirvmir.course.application.service.interfaces;

import io.github.mirvmir.course.web.request.*;
import io.github.mirvmir.course.web.response.AuthorCourseLessonResponse;
import io.github.mirvmir.course.web.response.AuthorCourseModuleResponse;
import io.github.mirvmir.course.web.response.AuthorCourseResponse;
import io.github.mirvmir.course.web.response.IdResponse;

import java.util.List;
import java.util.UUID;

public interface AuthorCourseService {
    List<AuthorCourseResponse> getAllCourse();
    AuthorCourseResponse getCourse(Long courseId);
    AuthorCourseModuleResponse getModule(Long courseId, UUID stableModuleId);
    AuthorCourseLessonResponse getLesson(Long courseId, UUID stableLessonId);
    IdResponse createCourse(CreateCourseRequest request);
    void updateTopics(Long courseId,
                      UpdateCourseTopicsRequest request);
    void updateLessonOpensAt(Long courseId,
                             UUID stableLessonId,
                             UpdateLessonOpensAtRequest request);
    AuthorCourseResponse updateDraftCourse(Long courseId,
                                           UpdateCourseDraftRequest request);
    AuthorCourseResponse saveDraftModules(Long courseId,
                                          SaveDraftModulesRequest request);
    AuthorCourseModuleResponse saveDraftModuleLessons(Long courseId,
                                                      Long moduleId,
                                                      SaveDraftModuleLessonsRequest request);
    AuthorCourseLessonResponse saveDraftLessonBlocks(Long courseId,
                                                     Long lessonId,
                                                     SaveDraftLessonBlocksRequest request);
    void requestPublication(Long courseId);
    void archive(Long courseId);
    void unarchive(Long courseId);
    void deleteCourse(Long courseId);
}