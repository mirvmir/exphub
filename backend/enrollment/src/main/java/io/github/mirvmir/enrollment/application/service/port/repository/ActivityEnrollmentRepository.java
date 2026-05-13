package io.github.mirvmir.enrollment.application.service.port.repository;

import io.github.mirvmir.enrollment.domain.ActivityEnrollment;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ActivityEnrollmentRepository {
    ActivityEnrollment findById(Long id);
    ActivityEnrollment saveOrUpdate(ActivityEnrollment enrollment);
    boolean existsActiveByUserIdAndActivitySlotId(Long userId,
                                                  Long activitySlotId,
                                                  Instant now);
    boolean existsPayedByUserIdAndActivityId(Long userId,
                                             Long activityId);
    Map<Long, Integer> countBookedByActivitySlotIds(Set<Long> activitySlotIds,
                                                    Instant now);
    ActivityEnrollment tryEnroll(Long activitySlotId,
                                 Long userId,
                                 Instant now);
    List<ActivityEnrollment> findActiveByActivitySlotId(Long activitySlotId);
    List<ActivityEnrollment> findPayedByActivitySlotId(Long activitySlotId);
    ActivityEnrollment findActiveByActivitySlotIdAndStudentId(Long activitySlotId,
                                                             Long studentId);
}
