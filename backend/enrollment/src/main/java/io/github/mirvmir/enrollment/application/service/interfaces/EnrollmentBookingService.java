package io.github.mirvmir.enrollment.application.service.interfaces;

import io.github.mirvmir.enrollment.web.request.BookIndividualActivityRequest;
import io.github.mirvmir.enrollment.web.response.BookingResponse;

public interface EnrollmentBookingService {
    BookingResponse bookCourse(Long courseId);
    BookingResponse bookGroupActivitySlot(Long activitySlotId);
    BookingResponse bookIndividualActivity(Long activityId,
                                           BookIndividualActivityRequest request);
}
