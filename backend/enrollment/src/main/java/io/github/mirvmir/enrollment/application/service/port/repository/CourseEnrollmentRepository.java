package io.github.mirvmir.enrollment.application.service.port.repository;

import io.github.mirvmir.enrollment.domain.CourseEnrollment;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface CourseEnrollmentRepository {
    CourseEnrollment findById(Long id);
    CourseEnrollment saveOrUpdate(CourseEnrollment enrollment);
    boolean existsPayedByUserIdAndCourseId(Long studentId, Long courseId);
    boolean existsPayedByUserIdAndCourseIds(Long userId,
                                            Set<Long> courseIds);
    boolean existsActiveByUserIdAndCourseId(Long userId,
                                            Long courseId,
                                            Instant now);
    List<CourseEnrollment> findActiveByCourseId(Long courseId);
    CourseEnrollment findPayedByUserIdAndCourseId(Long userId,
                                                  Long courseId);
    List<CourseEnrollment> findPayedByCourseId(Long courseId);
}
