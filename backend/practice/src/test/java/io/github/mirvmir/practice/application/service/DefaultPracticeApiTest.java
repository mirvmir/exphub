package io.github.mirvmir.practice.application.service;

import io.github.mirvmir.practice.application.service.port.repository.PracticeSubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

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
        when(practiceSubmissionRepository.existsCheckedByStableLessonIdAndStudentId(
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                1L))
                .thenReturn(true);

        boolean result =
                practiceApi.isPracticeCompletedByLessonIdAndStudentId(
                        UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                        1L
                );

        assertTrue(result);

        verify(practiceSubmissionRepository)
                .existsCheckedByStableLessonIdAndStudentId(
                        UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                        1L
                );
    }

    @Test
    void isPracticeCompletedByLessonIdAndStudentId_shouldReturnFalse() {
        when(practiceSubmissionRepository.existsCheckedByStableLessonIdAndStudentId(
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                1L))
                .thenReturn(false);

        boolean result =
                practiceApi.isPracticeCompletedByLessonIdAndStudentId(
                        UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                        1L
                );

        assertFalse(result);

        verify(practiceSubmissionRepository)
                .existsCheckedByStableLessonIdAndStudentId(
                        UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                        1L
                );
    }
}