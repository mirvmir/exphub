package io.github.mirvmir.practice.application.service;

import io.github.mirvmir.practice.api.PracticeApi;
import io.github.mirvmir.practice.application.service.port.repository.PracticeSubmissionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class DefaultPracticeApi implements PracticeApi {

    private final PracticeSubmissionRepository practiceSubmissionRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean isPracticeCompletedByLessonIdAndStudentId(
            Long courseLessonId,
            Long studentId
    ) {
        return practiceSubmissionRepository
                .existsCheckedByLessonIdAndStudentId(
                        courseLessonId,
                        studentId
                );
    }
}