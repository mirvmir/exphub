package io.github.mirvmir.enrollment.web.controller;

import io.github.mirvmir.enrollment.application.service.interfaces.EnrollmentBookingService;
import io.github.mirvmir.enrollment.web.request.BookIndividualActivityRequest;
import io.github.mirvmir.enrollment.web.response.BookingResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentBookingService enrollmentBookingService;

    @PostMapping("/courses/{courseId}/book")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse bookCourse(
            @PathVariable("courseId")
            Long courseId
    ) {
        return enrollmentBookingService.bookCourse(
                courseId
        );
    }

    @PostMapping("/activity-slots/{activitySlotId}/book")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse bookGroupActivitySlot(
            @PathVariable("activitySlotId")
            Long activitySlotId
    ) {
        return enrollmentBookingService.bookGroupActivitySlot(
                activitySlotId
        );
    }

    @PostMapping("/individual-activities/{activityId}/book")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse bookIndividualActivity(
            @PathVariable("activityId")
            Long activityId,
            @Valid
            @RequestBody
            BookIndividualActivityRequest request
    ) {
        return enrollmentBookingService.bookIndividualActivity(
                activityId,
                request
        );
    }
}