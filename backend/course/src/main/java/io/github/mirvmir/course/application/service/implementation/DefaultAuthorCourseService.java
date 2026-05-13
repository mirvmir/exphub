package io.github.mirvmir.course.application.service.implementation;

import io.github.mirvmir.common.exception.ForbiddenException;
import io.github.mirvmir.common.exception.NotFoundException;
import io.github.mirvmir.course.api.event.CourseChangeTopicIds;
import io.github.mirvmir.course.api.event.CourseDeleteEvent;
import io.github.mirvmir.course.api.event.CoursePublishedEvent;
import io.github.mirvmir.course.application.service.mapper.CourseResponseMapper;
import io.github.mirvmir.course.application.service.port.event.CourseEventPublisher;
import io.github.mirvmir.course.application.service.port.repository.CourseRepository;
import io.github.mirvmir.course.application.service.port.repository.CourseVersionRepository;
import io.github.mirvmir.course.application.service.interfaces.AuthorCourseService;
import io.github.mirvmir.course.domain.Course;
import io.github.mirvmir.course.domain.CourseLesson;
import io.github.mirvmir.course.domain.CourseModule;
import io.github.mirvmir.course.domain.CourseVersion;
import io.github.mirvmir.course.exception.CourseErrorCode;
import io.github.mirvmir.course.web.request.*;
import io.github.mirvmir.course.web.response.AuthorCourseLessonResponse;
import io.github.mirvmir.course.web.response.AuthorCourseModuleResponse;
import io.github.mirvmir.course.web.response.AuthorCourseResponse;
import io.github.mirvmir.course.web.response.IdResponse;
import io.github.mirvmir.identity.api.IdentityApi;
import io.github.mirvmir.profile.api.ProfileApi;
import io.github.mirvmir.profile.api.dto.ProfileNameDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@AllArgsConstructor
@Slf4j
@Service
public class DefaultAuthorCourseService implements AuthorCourseService {

    private final IdentityApi identityApi;
    private final ProfileApi profileApi;

    private final CourseRepository courseRepository;
    private final CourseVersionRepository courseVersionRepository;

    private final CourseResponseMapper courseResponseMapper;

    private final CourseEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public AuthorCourseResponse getCourse(Long courseId) {
        log.debug("Getting course with id={}", courseId);

        Course course = getExistingCourse(courseId);

        ensureAuthor(course);

        Long draftVersionId = getDraftVersionId(course);

        CourseVersion draftVersion =
                courseVersionRepository.findByIdAndCourseIdWithModules(
                        draftVersionId,
                        courseId
                );


        if (draftVersion == null) {
            log.error("Author course getting failed, draft version not found: courseId={}, draftVersionId={}",
                    courseId, draftVersionId);
            throw new NotFoundException(
                    CourseErrorCode.COURSE_DRAFT_VERSION_NOT_FOUND,
                    "Draft version for courseId=" + courseId + " not found"
            );
        }

        ProfileNameDto author =
                profileApi.getProfileName(course.getAuthorId());

        AuthorCourseResponse response = courseResponseMapper.toAuthorCourseResponse(
                course,
                draftVersion,
                author,
                course.isEditable(),
                course.isEditable()
        );

        log.info("Author course successfully received: courseId={}, draftVersionId={}",
                courseId, draftVersionId);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorCourseModuleResponse getModule(Long courseId,
                                                UUID stableModuleId) {
        log.debug("Getting course module with id={}", stableModuleId);

        Course course = getExistingCourse(courseId);

        ensureAuthor(course);

        Long draftVersionId = getDraftVersionId(course);

        CourseVersion draftVersion =
                courseVersionRepository.findByIdAndCourseIdWithModule(
                        draftVersionId,
                        courseId,
                        stableModuleId
                );

        if (draftVersion == null) {
            log.error("Author course module getting failed, module not found: courseId={}, draftVersionId={}, stableModuleId={}",
                    courseId, draftVersionId, stableModuleId);
            throw new NotFoundException(
                    CourseErrorCode.COURSE_MODULE_NOT_FOUND,
                    "Course module with stableModuleId=" + stableModuleId + " not found"
            );
        }

        CourseModule module =
                draftVersion.findModuleByStableId(stableModuleId);

        AuthorCourseModuleResponse response = courseResponseMapper.toAuthorCourseModuleResponse(
                module,
                course.isEditable()
        );

        log.info("Author course module successfully received: courseId={}, draftVersionId={}, stableModuleId={}",
                courseId, draftVersionId, stableModuleId);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AuthorCourseLessonResponse getLesson(Long courseId,
                                                UUID stableLessonId) {
        log.debug("Getting course lesson with id={}", stableLessonId);

        Course course = getExistingCourse(courseId);

        ensureAuthor(course);

        Long draftVersionId = getDraftVersionId(course);

        CourseVersion draftVersion =
                courseVersionRepository.findByIdAndCourseIdWithLesson(
                        draftVersionId,
                        courseId,
                        stableLessonId
                );

        if (draftVersion == null) {
            log.error("Author course lesson getting failed, lesson not found: courseId={}, draftVersionId={}, stableLessonId={}",
                    courseId, draftVersionId, stableLessonId);
            throw new NotFoundException(
                    CourseErrorCode.COURSE_LESSON_NOT_FOUND,
                    "Course lesson with stableLessonId=" + stableLessonId + " not found"
            );
        }

        CourseLesson lesson =
                draftVersion.findLessonByStableId(stableLessonId);

        AuthorCourseLessonResponse response = courseResponseMapper.toAuthorCourseLessonResponse(
                lesson,
                course.isEditable()
        );

        log.info("Author course lesson successfully received: courseId={}, draftVersionId={}, stableLessonId={}",
                courseId, draftVersionId, stableLessonId);

        return response;
    }

    @Override
    @Transactional
    public IdResponse createCourse(CreateCourseRequest request) {
        log.info("Course creation requested: title={}",
                request.title());

        Long authorId = identityApi.getCurrentUserId();

        Course course = Course.create(
                authorId,
                request.title()
        );

        Course savedCourse = courseRepository.saveOrUpdate(course);

        log.info("Course successfully created: courseId={}, authorId={}",
                savedCourse.getId(), authorId);

        return new IdResponse(savedCourse.getId());
    }

    @Override
    @Transactional
    public void updateTopics(Long courseId,
                             UpdateCourseTopicsRequest request) {
        log.info("Course topics update requested: courseId={}, topicIds={}",
                courseId, request.topicIds());

        Course course = getExistingCourse(courseId);
        ensureAuthor(course);

        course.updateTopics(request.topicIds());

        Course savedCourse = courseRepository.saveOrUpdate(course);
        eventPublisher.changeTopic(
                new CourseChangeTopicIds(
                        savedCourse.getId(),
                        savedCourse.getTopicIds()
                )
        );

        log.info("Course topics successfully updated: courseId={}, topicIds={}",
                savedCourse.getId(), savedCourse.getTopicIds());
    }

    @Override
    @Transactional
    public void updateLessonOpensAt(Long courseId,
                                    UUID stableLessonId,
                                    UpdateLessonOpensAtRequest request) {
        log.info("Course lesson open date update requested: courseId={}, stableLessonId={}, opensAt={}",
                courseId, stableLessonId, request.opensAt());

        Course course = getExistingCourse(courseId);

        ensureAuthor(course);

        course.updateLessonOpensAt(
                stableLessonId,
                request.opensAt()
        );

        courseRepository.saveOrUpdate(course);

        log.info("Course lesson open date successfully updated: courseId={}, stableLessonId={}, opensAt={}",
                courseId, stableLessonId, request.opensAt());
    }

    @Override
    @Transactional
    public AuthorCourseResponse updateDraftCourse(Long courseId,
                                                  UpdateCourseDraftRequest request) {
        log.info("Course draft update requested: courseId={}", courseId);

        Course course = getExistingCourseWithDraft(courseId);

        ensureAuthor(course);

        course.updateDraftCourse(
                request.title(),
                request.shortDescription(),
                request.descriptionHtml(),
                request.priceAmount(),
                request.priceCurrency()
        );

        Course savedCourse = courseRepository.saveOrUpdate(course);

        AuthorCourseResponse response = toAuthorCourseResponse(savedCourse);

        log.info("Course draft successfully updated: courseId={}, draftVersionId={}",
                savedCourse.getId(), savedCourse.getDraftVersion().getId());

        return response;
    }

    @Override
    @Transactional
    public AuthorCourseResponse saveDraftModules(Long courseId,
                                                 SaveDraftModulesRequest request) {
        log.info("Course draft modules saving requested: courseId={}", courseId);

        Course course = getExistingCourseWithDraft(courseId);

        ensureAuthor(course);

        course.saveDraftModules(request.modules());

        Course savedCourse = courseRepository.saveOrUpdate(course);

        CourseVersion draftVersion =
                courseVersionRepository.findByIdAndCourseIdWithModules(
                        savedCourse.getDraftVersion().getId(),
                        courseId
                );

        ProfileNameDto author =
                profileApi.getProfileName(savedCourse.getAuthorId());

        AuthorCourseResponse response = courseResponseMapper.toAuthorCourseResponse(
                savedCourse,
                draftVersion,
                author,
                savedCourse.isEditable(),
                savedCourse.isEditable()
        );

        log.info("Course draft modules successfully saved: courseId={}, draftVersionId={}",
                courseId, savedCourse.getDraftVersion().getId());

        return response;
    }

    @Override
    @Transactional
    public AuthorCourseModuleResponse saveDraftModuleLessons(Long courseId,
                                                             Long moduleId,
                                                             SaveDraftModuleLessonsRequest request) {
        log.info("Course draft module lessons saving requested: courseId={}, moduleId={}, stableModuleId={}",
                courseId, moduleId, request.stableModuleId());

        Course course = getExistingCourseWithDraft(courseId);

        ensureAuthor(course);

        course.saveDraftModuleLessons(
                moduleId,
                request.lessons()
        );

        Course savedCourse = courseRepository.saveOrUpdate(course);

        CourseVersion draftVersion =
                courseVersionRepository.findByIdAndCourseIdWithModule(
                        savedCourse.getDraftVersion().getId(),
                        courseId,
                        request.stableModuleId()
                );

        CourseModule module =
                draftVersion.findModuleByStableId(
                        request.stableModuleId()
                );

        AuthorCourseModuleResponse response = courseResponseMapper.toAuthorCourseModuleResponse(
                module,
                savedCourse.isEditable()
        );

        log.info("Course draft module lessons successfully saved: courseId={}, moduleId={}, stableModuleId={}",
                courseId, moduleId, request.stableModuleId());

        return response;
    }

    @Override
    @Transactional
    public AuthorCourseLessonResponse saveDraftLessonBlocks(Long courseId,
                                                            Long lessonId,
                                                            SaveDraftLessonBlocksRequest request) {
        log.info("Course draft lesson blocks saving requested: courseId={}, lessonId={}, stableLessonId={}",
                courseId, lessonId, request.stableLessonId());

        Course course = getExistingCourseWithDraft(courseId);

        ensureAuthor(course);

        course.saveDraftLessonBlocks(
                lessonId,
                request.blocks()
        );

        Course savedCourse = courseRepository.saveOrUpdate(course);

        CourseVersion draftVersion =
                courseVersionRepository.findByIdAndCourseIdWithLesson(
                        savedCourse.getDraftVersion().getId(),
                        courseId,
                        request.stableLessonId()
                );

        CourseLesson lesson =
                draftVersion.findLessonByStableId(
                        request.stableLessonId()
                );

        AuthorCourseLessonResponse response = courseResponseMapper.toAuthorCourseLessonResponse(
                lesson,
                savedCourse.isEditable()
        );

        log.info("Course draft lesson blocks successfully saved: courseId={}, lessonId={}, stableLessonId={}",
                courseId, lessonId, request.stableLessonId());

        return response;
    }

    @Override
    @Transactional
    public void requestPublication(Long courseId) {
        log.info("Course publication requested: courseId={}", courseId);

        Course course = getExistingCourseWithDraft(courseId);

        ensureAuthor(course);

        course.requestPublication();

        courseRepository.saveOrUpdate(course);

        log.info("Course publication successfully requested: courseId={}", courseId);
    }

    @Override
    @Transactional
    public void archive(Long courseId) {
        log.info("Course archiving requested: courseId={}", courseId);

        Course course = getExistingCourse(courseId);

        ensureAuthor(course);

        course.archive();

        courseRepository.saveOrUpdate(course);

        eventPublisher.delete(new CourseDeleteEvent(
                courseId
        ));

        log.info("Course successfully archived: courseId={}", courseId);
    }

    @Override
    @Transactional
    public void unarchive(Long courseId) {
        log.info("Course unarchiving requested: courseId={}", courseId);

        Course course = getExistingCourse(courseId);
        ensureAuthor(course);

        course.unarchive();

        courseRepository.saveOrUpdate(course);

        eventPublisher.publish(new CoursePublishedEvent(
                courseId,
                course.getAuthorId(),
                course.getPublishedVersion().getTitle(),
                course.getPublishedVersion().getShortDescription(),
                course.getPublishedVersion().getPrice().getAmount(),
                course.getPublishedVersion().getPrice().getCurrency(),
                course.getTopicIds()
        ));

        log.info("Course successfully unarchived: courseId={}", courseId);
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        log.info("Course deletion requested: courseId={}", courseId);

        Course course = getExistingCourse(courseId);

        ensureAuthor(course);

        course.delete();

        courseRepository.saveOrUpdate(course);

        eventPublisher.delete(new CourseDeleteEvent(
                courseId
        ));

        log.info("Course successfully deleted: courseId={}", courseId);
    }

    private Course getExistingCourse(Long courseId) {
        Course course = courseRepository.findById(courseId);

        if (course == null) {
            log.error("Course loading failed, course not found: courseId={}", courseId);
            throw new NotFoundException(
                    CourseErrorCode.COURSE_NOT_FOUND,
                    "Course with id=" + courseId + " not found"
            );
        }

        return course;
    }

    private Long getDraftVersionId(Course course) {
        if (course.getDraftVersion() == null
                || course.getDraftVersion().getId() == null) {
            log.error("Draft version loading failed, draft version not found: courseId={}",
                    course.getId());
            throw new NotFoundException(
                    CourseErrorCode.COURSE_DRAFT_VERSION_NOT_FOUND,
                    "Draft version not found"
            );
        }

        return course.getDraftVersion().getId();
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

    private void ensureAuthor(Course course) {
        Long currentUserId = identityApi.getCurrentUserId();

        if (!currentUserId.equals(course.getAuthorId())) {
            log.error("Course access denied: courseId={}, currentUserId={}, authorId={}",
                    course.getId(), currentUserId, course.getAuthorId());
            throw new ForbiddenException(CourseErrorCode.COURSE_FORBIDDEN);
        }
    }

    private AuthorCourseResponse toAuthorCourseResponse(Course course) {
        ProfileNameDto author = profileApi.getProfileName(course.getAuthorId());

        boolean canEdit = course.isEditable();
        boolean canPublication = course.isEditable();

        return courseResponseMapper.toAuthorCourseResponse(
                course,
                course.getDraftVersion(),
                author,
                canEdit,
                canPublication
        );
    }
}
