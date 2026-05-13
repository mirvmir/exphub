package io.github.mirvmir.practice.application.service;

import io.github.mirvmir.practice.application.service.port.repository.PracticeSubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultPracticeApiTest {

    private PracticeSubmissionRepository practiceSubmissionRepository;
    private DefaultPracticeApi practiceApi;

    @BeforeEach
    void setUp() {
        practiceSubmissionRepository = mock(PracticeSubmissionRepository.class);

        practiceApi = new DefaultPracticeApi(practiceSubmissionRepository);
    }

    @Test
    void isPracticeCompletedByLessonIdAndStudentId_shouldReturnTrue() {
        when(practiceSubmissionRepository.existsCheckedByLessonIdAndStudentId(100L, 1L))
                .thenReturn(true);

        boolean result =
                practiceApi.isPracticeCompletedByLessonIdAndStudentId(100L, 1L);

        assertTrue(result);

        verify(practiceSubmissionRepository)
                .existsCheckedByLessonIdAndStudentId(100L, 1L);
    }

    @Test
    void isPracticeCompletedByLessonIdAndStudentId_shouldReturnFalse() {
        when(practiceSubmissionRepository.existsCheckedByLessonIdAndStudentId(100L, 1L))
                .thenReturn(false);

        boolean result =
                practiceApi.isPracticeCompletedByLessonIdAndStudentId(100L, 1L);

        assertFalse(result);

        verify(practiceSubmissionRepository)
                .existsCheckedByLessonIdAndStudentId(100L, 1L);
    }
}