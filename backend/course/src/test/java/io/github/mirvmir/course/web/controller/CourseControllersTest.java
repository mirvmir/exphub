package io.github.mirvmir.course.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.mirvmir.common.domain.ContentStatus;
import io.github.mirvmir.common.exception.GlobalExceptionHandler;
import io.github.mirvmir.course.application.service.interfaces.AuthorCourseService;
import io.github.mirvmir.course.application.service.interfaces.CourseService;
import io.github.mirvmir.course.application.service.interfaces.ModerationCourseService;
import io.github.mirvmir.course.application.service.interfaces.StudentCourseService;
import io.github.mirvmir.course.domain.LessonType;
import io.github.mirvmir.course.domain.content.LessonContentType;
import io.github.mirvmir.course.web.request.*;
import io.github.mirvmir.course.web.response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CourseControllersTest {

    private CourseService courseService;
    private AuthorCourseService authorCourseService;
    private StudentCourseService studentCourseService;
    private ModerationCourseService moderationCourseService;

    private MockMvc courseMockMvc;
    private MockMvc authorMockMvc;
    private MockMvc studentMockMvc;
    private MockMvc moderationMockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        courseService = mock(CourseService.class);
        authorCourseService = mock(AuthorCourseService.class);
        studentCourseService = mock(StudentCourseService.class);
        moderationCourseService = mock(ModerationCourseService.class);

        courseMockMvc = MockMvcBuilders
                .standaloneSetup(new CourseController(courseService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        authorMockMvc = MockMvcBuilders
                .standaloneSetup(new AuthorCourseController(authorCourseService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        studentMockMvc = MockMvcBuilders
                .standaloneSetup(new StudentCourseController(studentCourseService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        moderationMockMvc = MockMvcBuilders
                .standaloneSetup(new ModerationCourseController(moderationCourseService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void getCourseById_shouldReturn200() throws Exception {
        CourseInfoResponse response = new CourseInfoResponse(
                1L,
                2L,
                null,
                "Курс",
                "Кратко",
                "<p>Описание</p>",
                new BigDecimal("3000"),
                Currency.getInstance("RUB"),
                Set.of(11L),
                false
        );

        when(courseService.getCourse(1L)).thenReturn(response);

        courseMockMvc.perform(get("/courses/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(courseService).getCourse(1L);
    }

    @Test
    void createCourse_shouldReturn200() throws Exception {
        CreateCourseRequest request = new CreateCourseRequest("Новый курс");

        when(authorCourseService.createCourse(any()))
                .thenReturn(new IdResponse(1L));

        authorMockMvc.perform(post("/author/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(authorCourseService).createCourse(argThat(actual ->
                actual != null
                        && actual.title().equals("Новый курс")
        ));
    }

    @Test
    void getAuthorCourse_shouldReturn200() throws Exception {
        AuthorCourseResponse response = new AuthorCourseResponse(
                1L,
                10L,
                2L,
                null,
                "Курс",
                "Кратко",
                "<p>Описание</p>",
                new BigDecimal("3000"),
                Currency.getInstance("RUB"),
                ContentStatus.DRAFT,
                Set.of(11L),
                true,
                true,
                List.of()
        );

        when(authorCourseService.getCourse(1L)).thenReturn(response);

        authorMockMvc.perform(get("/author/courses/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(authorCourseService).getCourse(1L);
    }

    @Test
    void updateDraftCourse_shouldReturn200() throws Exception {
        UpdateCourseDraftRequest request = new UpdateCourseDraftRequest(
                "Новое название",
                "Новое кратко",
                "<p>Новое описание</p>",
                new BigDecimal("4500"),
                Currency.getInstance("RUB")
        );

        when(authorCourseService.updateDraftCourse(eq(1L), any()))
                .thenReturn(mock(AuthorCourseResponse.class));

        authorMockMvc.perform(patch("/author/courses/1/draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authorCourseService).updateDraftCourse(eq(1L), argThat(actual ->
                actual != null
                        && actual.title().equals("Новое название")
                        && actual.priceAmount().equals(new BigDecimal("4500"))
        ));
    }

    @Test
    void updateTopics_shouldReturn200() throws Exception {
        UpdateCourseTopicsRequest request =
                new UpdateCourseTopicsRequest(3L, Set.of(11L, 12L));

        authorMockMvc.perform(patch("/author/courses/1/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(authorCourseService).updateTopics(eq(1L), argThat(actual ->
                actual != null
                        && actual.topicIds().equals(Set.of(11L, 12L))
        ));
    }

    @Test
    void updateLessonOpensAt_shouldReturn200() throws Exception {
        UUID stableLessonId = UUID.randomUUID();
        Instant opensAt = Instant.now().plusSeconds(3600);

        UpdateLessonOpensAtRequest request =
                new UpdateLessonOpensAtRequest(opensAt);

        authorMockMvc.perform(patch("/author/courses/1/lessons/" + stableLessonId + "/opens-at")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(authorCourseService).updateLessonOpensAt(
                eq(1L),
                eq(stableLessonId),
                argThat(actual -> actual.opensAt().equals(opensAt))
        );
    }

    @Test
    void saveDraftModules_shouldReturn200() throws Exception {
        SaveDraftModulesRequest request = new SaveDraftModulesRequest(
                List.of(new SaveDraftModuleItemRequest(
                        null,
                        "module-ui-id",
                        "Модуль 1",
                        0
                ))
        );

        when(authorCourseService.saveDraftModules(eq(1L), any()))
                .thenReturn(mock(AuthorCourseResponse.class));

        authorMockMvc.perform(put("/author/courses/1/draft/modules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authorCourseService).saveDraftModules(eq(1L), argThat(actual ->
                actual.modules().size() == 1
                        && actual.modules().get(0).title().equals("Модуль 1")
        ));
    }

    @Test
    void saveDraftModuleLessons_shouldReturn200() throws Exception {
        UUID stableModuleId = UUID.randomUUID();

        SaveDraftModuleLessonsRequest request =
                new SaveDraftModuleLessonsRequest(
                        List.of(new SaveDraftLessonItemRequest(
                                null,
                                "lesson-ui-id",
                                "Урок 1",
                                LessonType.THEORY,
                                0
                        )),
                        stableModuleId
                );

        when(authorCourseService.saveDraftModuleLessons(eq(1L), eq(2L), any()))
                .thenReturn(mock(AuthorCourseModuleResponse.class));

        authorMockMvc.perform(put("/author/courses/1/draft/modules/2/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authorCourseService).saveDraftModuleLessons(eq(1L), eq(2L), argThat(actual ->
                actual.stableModuleId().equals(stableModuleId)
                        && actual.lessons().get(0).title().equals("Урок 1")
        ));
    }

    @Test
    void saveDraftLessonBlocks_shouldReturn200() throws Exception {
        UUID stableLessonId = UUID.randomUUID();

        SaveDraftLessonBlocksRequest request =
                new SaveDraftLessonBlocksRequest(
                        List.of(new SaveDraftLessonBlockItemRequest(
                                null,
                                "block-ui-id",
                                LessonContentType.HTML,
                                "<p>Текст</p>",
                                null,
                                null,
                                0
                        )),
                        stableLessonId
                );

        when(authorCourseService.saveDraftLessonBlocks(eq(1L), eq(3L), any()))
                .thenReturn(mock(AuthorCourseLessonResponse.class));

        authorMockMvc.perform(put("/author/courses/1/draft/lessons/3/blocks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authorCourseService).saveDraftLessonBlocks(eq(1L), eq(3L), argThat(actual ->
                actual.stableLessonId().equals(stableLessonId)
                        && actual.blocks().get(0).html().equals("<p>Текст</p>")
        ));
    }

    @Test
    void requestPublication_shouldReturn200() throws Exception {
        authorMockMvc.perform(post("/author/courses/1/publication-request"))
                .andExpect(status().isNoContent());

        verify(authorCourseService).requestPublication(1L);
    }

    @Test
    void archive_shouldReturn200() throws Exception {
        authorMockMvc.perform(post("/author/courses/1/archive"))
                .andExpect(status().isNoContent());

        verify(authorCourseService).archive(1L);
    }

    @Test
    void unarchive_shouldReturn200() throws Exception {
        authorMockMvc.perform(post("/author/courses/1/unarchive"))
                .andExpect(status().isNoContent());

        verify(authorCourseService).unarchive(1L);
    }

    @Test
    void deleteCourse_shouldReturn200() throws Exception {
        authorMockMvc.perform(delete("/author/courses/1"))
                .andExpect(status().isNoContent());

        verify(authorCourseService).deleteCourse(1L);
    }

    @Test
    void getStudentCourse_shouldReturn200() throws Exception {
        when(studentCourseService.getCourse(1L))
                .thenReturn(mock(StudentCourseResponse.class));

        studentMockMvc.perform(get("/student/courses/1"))
                .andExpect(status().isOk());

        verify(studentCourseService).getCourse(1L);
    }

    @Test
    void getStudentCourseDescription_shouldReturn200() throws Exception {
        when(studentCourseService.getCourseDescription(1L))
                .thenReturn(mock(CourseInfoResponse.class));

        studentMockMvc.perform(get("/student/courses/1/description"))
                .andExpect(status().isOk());

        verify(studentCourseService).getCourseDescription(1L);
    }

    @Test
    void getStudentModule_shouldReturn200() throws Exception {
        UUID stableModuleId = UUID.randomUUID();

        when(studentCourseService.getModule(1L, stableModuleId))
                .thenReturn(mock(StudentCourseModuleResponse.class));

        studentMockMvc.perform(get("/student/courses/1/modules/" + stableModuleId))
                .andExpect(status().isOk());

        verify(studentCourseService).getModule(1L, stableModuleId);
    }

    @Test
    void getStudentLesson_shouldReturn200() throws Exception {
        UUID stableLessonId = UUID.randomUUID();

        when(studentCourseService.getLesson(1L, stableLessonId))
                .thenReturn(mock(StudentCourseLessonResponse.class));

        studentMockMvc.perform(get("/student/courses/1/lessons/" + stableLessonId))
                .andExpect(status().isOk());

        verify(studentCourseService).getLesson(1L, stableLessonId);
    }

    @Test
    void approve_shouldReturn200() throws Exception {
        moderationMockMvc.perform(post("/moderation/courses/1/approve"))
                .andExpect(status().isOk());

        verify(moderationCourseService).approve(1L);
    }

    @Test
    void reject_shouldReturn200() throws Exception {
        RejectCourseRequest request =
                new RejectCourseRequest("Нужно доработать описание");

        moderationMockMvc.perform(post("/moderation/courses/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(moderationCourseService).reject(eq(1L), argThat(actual ->
                actual.moderationComment().equals("Нужно доработать описание")
        ));
    }

    @Test
    void block_shouldReturn200() throws Exception {
        moderationMockMvc.perform(post("/moderation/courses/1/block"))
                .andExpect(status().isOk());

        verify(moderationCourseService).block(1L);
    }
}